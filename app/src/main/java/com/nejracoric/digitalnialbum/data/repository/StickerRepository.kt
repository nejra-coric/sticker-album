package com.nejracoric.digitalnialbum.data.repository

import android.content.Context
import com.nejracoric.digitalnialbum.data.local.AppDatabase
import com.nejracoric.digitalnialbum.data.local.entity.FavoriteEntity
import com.nejracoric.digitalnialbum.data.local.entity.OwnedStickerEntity
import com.nejracoric.digitalnialbum.data.local.entity.StickerEntity
import com.nejracoric.digitalnialbum.data.local.entity.WishlistEntity
import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.data.model.TeamProgress
import com.nejracoric.digitalnialbum.data.remote.RetrofitClient
import com.nejracoric.digitalnialbum.data.remote.dto.PlayerDto
import com.nejracoric.digitalnialbum.data.remote.dto.TeamDto
import com.nejracoric.digitalnialbum.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

sealed class RepoResult<out T> {
    data class Success<T>(val data: T) : RepoResult<T>()
    data class Error(val message: String) : RepoResult<Nothing>()
}

class StickerRepository(
    context: Context,
    private val db: AppDatabase = AppDatabase.get(context),
    private val networkMonitor: NetworkMonitor = NetworkMonitor(context)
) {
    private val dao = db.stickerDao()

    val isOnline = networkMonitor.isOnline

    val stickers: Flow<List<Sticker>> = combine(
        dao.observeCatalog(),
        dao.observeOwned(),
        dao.observeFavorites(),
        dao.observeWishlist()
    ) { catalog, owned, favorites, wishes ->
        val ownedGroups = owned.groupBy { it.stickerId }
        val favSet = favorites.map { it.stickerId }.toSet()
        val wishSet = wishes.map { it.stickerId }.toSet()
        catalog.map { entity ->
            val copies = ownedGroups[entity.id]?.size ?: 0
            entity.toModel(
                owned = copies > 0,
                ownedCount = copies,
                obtainedAt = ownedGroups[entity.id]?.maxOfOrNull { it.obtainedAt },
                isFavorite = favSet.contains(entity.id),
                isWished = wishSet.contains(entity.id)
            )
        }
    }

    val ownedCount: Flow<Int> = dao.observeOwnedCount()
    val totalCount: Flow<Int> = dao.observeTotalCount()

    val favorites: Flow<List<Sticker>> = combine(stickers, dao.observeFavorites()) { all, favs ->
        val ids = favs.map { it.stickerId }.toSet()
        all.filter { ids.contains(it.id) }
    }

    val wishlist: Flow<List<Sticker>> = combine(stickers, dao.observeWishlist()) { all, wishes ->
        val ids = wishes.map { it.stickerId }.toSet()
        all.filter { ids.contains(it.id) }
    }

    val duplicates: Flow<List<Sticker>> = stickers.map { list ->
        list.filter { it.ownedCount > 1 }
    }

    suspend fun refreshFromNetwork(): RepoResult<Unit> {
        return try {
            val remote = RetrofitClient.api.getAllPlayers()
            if (remote.isEmpty()) {
                RepoResult.Error("API je vratio praznu listu.")
            } else {
                dao.insertAll(remote.map { it.toEntity() })
                RepoResult.Success(Unit)
            }
        } catch (e: Exception) {
            if (dao.catalogCount() == 0) {
                RepoResult.Error("Nema interneta i nema keširanih podataka.")
            } else {
                RepoResult.Error("Offline – prikaz keširanih podataka.")
            }
        }
    }

    suspend fun openPack(): RepoResult<List<Sticker>> {
        return try {
            val pack = RetrofitClient.api.getRandomPack(5)
            if (pack.isEmpty()) {
                RepoResult.Error("Paketić je prazan.")
            } else {
                val now = System.currentTimeMillis()
                dao.insertAll(pack.map { it.toEntity() })
                dao.insertOwnedAll(
                    pack.map { OwnedStickerEntity(stickerId = it.id, obtainedAt = now) }
                )
                val result = pack.map { dto ->
                    dto.toEntity().toModel(
                        owned = true,
                        ownedCount = dao.ownedCountFor(dto.id),
                        obtainedAt = now,
                        isFavorite = dao.isFavorite(dto.id),
                        isWished = dao.isWished(dto.id)
                    )
                }
                RepoResult.Success(result)
            }
        } catch (e: Exception) {
            RepoResult.Error(e.message ?: "Nije moguće otvoriti paketić")
        }
    }

    suspend fun getTeams(): RepoResult<List<TeamDto>> {
        return try {
            RepoResult.Success(RetrofitClient.api.getTeams())
        } catch (e: Exception) {
            RepoResult.Error(e.message ?: "Greška pri učitavanju timova")
        }
    }

    suspend fun getTeamProgress(): List<TeamProgress> {
        val teams = try {
            RetrofitClient.api.getTeams()
        } catch (_: Exception) {
            emptyList()
        }
        val all = stickers.first()
        return teams.map { team ->
            val teamStickers = all.filter { it.team == team.reprezentacija }
            TeamProgress(
                code = team.code(),
                name = team.reprezentacija,
                crestUrl = team.crestUrl(),
                collected = teamStickers.count { it.owned },
                total = teamStickers.size
            )
        }.sortedByDescending { it.percent }
    }

    suspend fun getSticker(id: Int): Sticker? {
        val entity = dao.getById(id) ?: return null
        return entity.toModel(
            owned = dao.isOwned(id),
            ownedCount = dao.ownedCountFor(id),
            obtainedAt = dao.obtainedAt(id),
            isFavorite = dao.isFavorite(id),
            isWished = dao.isWished(id)
        )
    }

    suspend fun toggleFavorite(stickerId: Int, currentlyFavorite: Boolean) {
        if (currentlyFavorite) dao.removeFavorite(stickerId)
        else dao.addFavorite(FavoriteEntity(stickerId))
    }

    suspend fun toggleWishlist(stickerId: Int, currentlyWished: Boolean) {
        if (currentlyWished) dao.removeWish(stickerId)
        else dao.addWish(WishlistEntity(stickerId))
    }

    private fun PlayerDto.toEntity() = StickerEntity(
        id = id,
        name = fullName(),
        number = broj_dresa.toString(),
        team = reprezentacija,
        position = pozicijaLabel(pozicija),
        rarity = rarityLabel(),
        imageUrl = imageUrl(),
        isGolden = zlatna == true
    )

    private fun pozicijaLabel(pozicija: String) = when (pozicija) {
        "GOALKEEPER" -> "Golman"
        "DEFENDER" -> "Odbrana"
        "MIDFIELDER" -> "Vezni red"
        "FORWARD" -> "Napadač"
        else -> pozicija
    }

    private fun StickerEntity.toModel(
        owned: Boolean,
        ownedCount: Int,
        obtainedAt: Long?,
        isFavorite: Boolean,
        isWished: Boolean
    ) = Sticker(
        id = id,
        name = name,
        number = number,
        team = team,
        position = position,
        rarity = rarity,
        imageUrl = imageUrl,
        isGolden = isGolden,
        owned = owned,
        ownedCount = ownedCount,
        obtainedAt = obtainedAt,
        isFavorite = isFavorite,
        isWished = isWished
    )
}

