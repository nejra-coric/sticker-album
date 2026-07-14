package com.nejracoric.digitalnialbum.data.repository

import android.content.Context
import com.nejracoric.digitalnialbum.data.local.AppDatabase
import com.nejracoric.digitalnialbum.data.local.entity.FavoriteEntity
import com.nejracoric.digitalnialbum.data.local.entity.OwnedStickerEntity
import com.nejracoric.digitalnialbum.data.local.entity.StickerEntity
import com.nejracoric.digitalnialbum.data.local.entity.WishlistEntity
import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.data.model.TeamProgress
import com.nejracoric.digitalnialbum.data.remote.ApiConfig
import com.nejracoric.digitalnialbum.data.remote.RetrofitClient
import com.nejracoric.digitalnialbum.data.remote.dto.PlayerDto
import com.nejracoric.digitalnialbum.data.remote.dto.TeamDto
import com.nejracoric.digitalnialbum.util.ImageCache
import com.nejracoric.digitalnialbum.util.NetworkMonitor
import com.nejracoric.digitalnialbum.util.buildCrestMap
import com.nejracoric.digitalnialbum.util.rarityFromDrawChance
import com.nejracoric.digitalnialbum.util.rarityRank
import com.nejracoric.digitalnialbum.util.resolveCrestUrl
import com.nejracoric.digitalnialbum.util.teamCodeFromName
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

sealed class RepoResult<out T> {
    data class Success<T>(val data: T) : RepoResult<T>()
    data class Error(val message: String) : RepoResult<Nothing>()
}

class StickerRepository(
    context: Context,
    private val db: AppDatabase = AppDatabase.get(context),
    private val networkMonitor: NetworkMonitor = NetworkMonitor(context)
) {
    private val appContext = context.applicationContext
    private val dao = db.stickerDao()
    private val _crestUrls = MutableStateFlow(defaultCrestMap())
    val crestUrls: StateFlow<Map<String, String>> = _crestUrls.asStateFlow()

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

    /**
     * Učitava podatke iz mreže samo ako je keš prazan ili je force=true (pull-to-refresh).
     * API je limitiran na 100 poziva/sat — ne zovi API pri svakom otvaranju app-a.
     */
    suspend fun refreshFromNetwork(force: Boolean = false): RepoResult<Unit> {
        val cached = dao.catalogCount()
        ensureDefaultCrests()

        if (!force && cached > 0) {
            applyChanceRaritiesWhereMissing()
            return RepoResult.Success(Unit)
        }

        return try {
            val remote = RetrofitClient.api.getAllPlayers()
            if (remote.isEmpty()) {
                RepoResult.Error("API je vratio praznu listu.")
            } else {
                upsertCatalogPreservingRarity(remote)
                applyChanceRaritiesWhereMissing()
                tryRefreshCrestsFromApi()
                prefetchCachedImages(includeFullCatalog = true)
                RepoResult.Success(Unit)
            }
        } catch (e: Exception) {
            when {
                cached == 0 -> RepoResult.Error(apiErrorMessage(e, "Nema interneta i nema keširanih podataka."))
                else -> {
                    applyChanceRaritiesWhereMissing()
                    RepoResult.Error(apiErrorMessage(e, "Prikaz keširanih podataka."))
                }
            }
        }
    }

    /** Za katalog bez tipa: dodijeli raritet po stabilnoj šansi (id), ne diraj već otkrivene GOLD/LEGEND. */
    private suspend fun applyChanceRaritiesWhereMissing() {
        val catalog = dao.observeCatalog().first()
        if (catalog.isEmpty()) return
        val updated = catalog.map { entity ->
            if (rarityRank(entity.rarity, entity.isGolden) > 0) return@map entity
            val label = rarityFromDrawChance(playerId = entity.id)
            entity.copy(
                rarity = label,
                isGolden = label.contains("zlat", true) || label.contains("legend", true)
            )
        }
        if (updated != catalog) dao.insertAll(updated)
    }

    private suspend fun tryRefreshCrestsFromApi() {
        try {
            _crestUrls.value = buildCrestMap(RetrofitClient.api.getTeams())
        } catch (_: Exception) {
            ensureDefaultCrests()
        }
    }

    private fun ensureDefaultCrests() {
        if (_crestUrls.value.isEmpty()) {
            _crestUrls.value = defaultCrestMap()
        }
    }

    suspend fun getLastOpenedPack(): List<Sticker> {
        val ts = dao.latestObtainedAt() ?: return emptyList()
        val ids = dao.stickerIdsAt(ts)
        if (ids.isEmpty()) return emptyList()
        val all = stickers.first()
        return ids.mapNotNull { id -> all.find { it.id == id } }
    }

    suspend fun openPack(): RepoResult<List<Sticker>> {
        return try {
            val pack = RetrofitClient.api.getRandomPack(5).filter { it.isPlayerCard() }
            if (pack.isEmpty()) {
                RepoResult.Error("Paketić je prazan.")
            } else {
                val now = System.currentTimeMillis()
                db.withTransaction {
                    upsertCatalogPreservingRarity(pack, preferIncomingRarity = true)
                    dao.insertOwnedAll(
                        pack.map { OwnedStickerEntity(stickerId = it.id, obtainedAt = now) }
                    )
                }
                val result = pack.map { dto ->
                    val entity = dao.getById(dto.id) ?: dto.toEntity()
                    entity.toModel(
                        owned = true,
                        ownedCount = dao.ownedCountFor(dto.id),
                        obtainedAt = now,
                        isFavorite = dao.isFavorite(dto.id),
                        isWished = dao.isWished(dto.id)
                    )
                }
                ImageCache.prefetchStickers(
                    appContext,
                    pack.map { it.id to it.imageUrl().ifBlank { ApiConfig.stickerImageUrl(it.id) } }
                )
                RepoResult.Success(result)
            }
        } catch (e: Exception) {
            RepoResult.Error(apiErrorMessage(e, "Nije moguće otvoriti paketić."))
        }
    }

    /**
     * /api/all-players nema tip_slicice — zato čuvamo rijetkost otkrivenu iz paketića.
     * Paketić šalje tip_slicice (obicna/zlatna/rijedka/legendarna).
     */
    private suspend fun upsertCatalogPreservingRarity(
        remote: List<PlayerDto>,
        preferIncomingRarity: Boolean = false
    ) {
        val existing = dao.observeCatalog().first().associateBy { it.id }
        val merged = remote.map { dto ->
            val incoming = dto.toEntity()
            val old = existing[incoming.id] ?: return@map incoming
            val incomingRank = rarityRank(incoming.rarity, incoming.isGolden)
            val oldRank = rarityRank(old.rarity, old.isGolden)
            when {
                preferIncomingRarity && incomingRank >= oldRank -> incoming
                oldRank > incomingRank -> incoming.copy(
                    rarity = old.rarity,
                    isGolden = old.isGolden || incoming.isGolden
                )
                else -> incoming
            }
        }
        dao.insertAll(merged)
    }

    suspend fun getTeams(): RepoResult<List<TeamDto>> {
        return try {
            RepoResult.Success(RetrofitClient.api.getTeams())
        } catch (e: Exception) {
            RepoResult.Error(apiErrorMessage(e, "Greška pri učitavanju timova."))
        }
    }

    suspend fun getTeamProgress(): List<TeamProgress> {
        val all = stickers.first()
        return all.map { it.team }.distinct().sorted().map { teamName ->
            val teamStickers = all.filter { it.team == teamName }
            TeamProgress(
                code = teamCodeFromName(teamName),
                name = teamName,
                crestUrl = resolveCrestUrl(teamName, _crestUrls.value),
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

    /** Preuzima grbove i sličice u lokalni keš. Pri startu samo skupljene; nakon synca cijeli katalog. */
    suspend fun prefetchCachedImages(includeFullCatalog: Boolean = false) {
        val catalog = dao.observeCatalog().first()
        if (catalog.isEmpty()) return
        val ownedIds = dao.observeOwned().first().map { it.stickerId }.toSet()
        val owned = catalog.filter { ownedIds.contains(it.id) }
            .map { it.id to it.imageUrl }
        ImageCache.prefetchStickers(appContext, owned)
        if (includeFullCatalog) {
            val rest = catalog.filterNot { ownedIds.contains(it.id) }
                .map { it.id to it.imageUrl }
            ImageCache.prefetchStickers(appContext, rest)
        }
        ImageCache.prefetchCrests(appContext, _crestUrls.value.values)
    }

    private fun apiErrorMessage(e: Exception, fallback: String): String {
        if (e is HttpException && e.code() == 429) {
            return "API limit iscrpljen (100 poziva/sat). $fallback"
        }
        return e.message?.takeIf { it.isNotBlank() } ?: fallback
    }

    private fun PlayerDto.toEntity() = StickerEntity(
        id = id,
        name = fullName(),
        number = broj_dresa.toString(),
        team = reprezentacija,
        position = pozicijaLabel(pozicija),
        rarity = rarityLabel(),
        imageUrl = imageUrl().ifBlank { ApiConfig.stickerImageUrl(id) },
        isGolden = isGoldenCard()
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
        imageUrl = imageUrl.ifBlank { ApiConfig.stickerImageUrl(id) },
        isGolden = isGolden,
        owned = owned,
        ownedCount = ownedCount,
        obtainedAt = obtainedAt,
        isFavorite = isFavorite,
        isWished = isWished
    )

    companion object {
        fun defaultCrestMap(): Map<String, String> {
            val codes = listOf(
                "ARG", "AUT", "BEL", "BIH", "BRA", "EGY", "ENG", "FRA",
                "CRO", "MAR", "NED", "GER", "NOR", "POR", "TUR"
            )
            return codes.associateWith { ApiConfig.crestImageUrl(it) }
        }
    }
}
