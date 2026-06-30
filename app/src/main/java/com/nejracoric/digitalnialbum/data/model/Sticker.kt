package com.nejracoric.digitalnialbum.data.model

data class Sticker(
    val id: Int,
    val name: String,
    val number: String,
    val team: String,
    val position: String,
    val rarity: String,
    val imageUrl: String,
    val isGolden: Boolean,
    val owned: Boolean,
    val ownedCount: Int,
    val obtainedAt: Long?,
    val isFavorite: Boolean,
    val isWished: Boolean
)

data class TeamProgress(
    val code: String,
    val name: String,
    val crestUrl: String,
    val collected: Int,
    val total: Int
) {
    val percent: Int get() = if (total == 0) 0 else collected * 100 / total
}

enum class ListLayout { LISTA, MREZA }

enum class SortOption { BROJ, NAZIV, DATUM, RIJETKOST }

enum class FilterOwned { SVE, SKUPLJENE, NEDOSTAJU, DUPLIKATI }
