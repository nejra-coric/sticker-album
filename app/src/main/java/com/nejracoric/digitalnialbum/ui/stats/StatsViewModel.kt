package com.nejracoric.digitalnialbum.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.data.repository.StickerRepository
import com.nejracoric.digitalnialbum.ui.components.ChartSlice
import com.nejracoric.digitalnialbum.ui.components.collectionDuplicateExtras
import com.nejracoric.digitalnialbum.ui.components.rarityBars
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val collected: Int = 0,
    val total: Int = 0,
    val duplicateExtras: Int = 0,
    val favorites: List<Sticker> = emptyList(),
    val rarityBars: List<ChartSlice> = emptyList()
) {
    val missing: Int get() = (total - collected).coerceAtLeast(0)
    val percent: Int get() = if (total == 0) 0 else (collected * 100) / total
}

class StatsViewModel(
    repository: StickerRepository
) : ViewModel() {

    val state = combine(
        repository.ownedCount,
        repository.totalCount,
        repository.favorites,
        repository.stickers
    ) { owned, total, favs, all ->
        StatsUiState(
            collected = owned,
            total = total,
            duplicateExtras = collectionDuplicateExtras(all),
            favorites = favs,
            rarityBars = rarityBars(all)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())
}

class StatsViewModelFactory(
    private val repository: StickerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatsViewModel(repository) as T
    }
}
