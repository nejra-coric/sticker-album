package com.nejracoric.digitalnialbum.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nejracoric.digitalnialbum.data.local.entity.FavoriteEntity
import com.nejracoric.digitalnialbum.data.local.entity.OwnedStickerEntity
import com.nejracoric.digitalnialbum.data.local.entity.StickerEntity
import com.nejracoric.digitalnialbum.data.local.entity.WishlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StickerDao {
    @Query("SELECT * FROM stickers ORDER BY CAST(number AS INTEGER) ASC")
    fun observeCatalog(): Flow<List<StickerEntity>>

    @Query("SELECT COUNT(*) FROM stickers")
    suspend fun catalogCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stickers: List<StickerEntity>)

    @Query("SELECT * FROM stickers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): StickerEntity?

    @Query("SELECT * FROM owned_stickers")
    fun observeOwned(): Flow<List<OwnedStickerEntity>>

    @Insert
    suspend fun insertOwned(item: OwnedStickerEntity)

    @Insert
    suspend fun insertOwnedAll(items: List<OwnedStickerEntity>)

    @Query("SELECT COUNT(*) FROM owned_stickers WHERE stickerId = :id")
    suspend fun ownedCountFor(id: Int): Int

    @Query("SELECT MAX(obtainedAt) FROM owned_stickers WHERE stickerId = :id")
    suspend fun obtainedAt(id: Int): Long?

    @Query("SELECT EXISTS(SELECT 1 FROM owned_stickers WHERE stickerId = :id)")
    suspend fun isOwned(id: Int): Boolean

    @Query("SELECT * FROM favorites")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(item: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE stickerId = :stickerId")
    suspend fun removeFavorite(stickerId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE stickerId = :id)")
    suspend fun isFavorite(id: Int): Boolean

    @Query("SELECT * FROM wishlist")
    fun observeWishlist(): Flow<List<WishlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addWish(item: WishlistEntity)

    @Query("DELETE FROM wishlist WHERE stickerId = :stickerId")
    suspend fun removeWish(stickerId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist WHERE stickerId = :id)")
    suspend fun isWished(id: Int): Boolean

    @Query("SELECT COUNT(DISTINCT stickerId) FROM owned_stickers")
    fun observeOwnedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM stickers")
    fun observeTotalCount(): Flow<Int>
}
