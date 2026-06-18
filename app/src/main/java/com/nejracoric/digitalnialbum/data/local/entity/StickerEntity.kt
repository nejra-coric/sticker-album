package com.nejracoric.digitalnialbum.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stickers")
data class StickerEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val number: String,
    val team: String,
    val position: String,
    val rarity: String,
    val imageUrl: String,
    val isGolden: Boolean
)

@Entity(tableName = "owned_stickers")
data class OwnedStickerEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    val stickerId: Int,
    val obtainedAt: Long
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val stickerId: Int
)

@Entity(tableName = "wishlist")
data class WishlistEntity(
    @PrimaryKey val stickerId: Int
)
