package com.nejracoric.digitalnialbum.ui.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nejracoric.digitalnialbum.data.model.FilterOwned
import com.nejracoric.digitalnialbum.data.model.ListLayout
import com.nejracoric.digitalnialbum.data.model.SortOption
import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.data.preferences.UserPreferences
import com.nejracoric.digitalnialbum.data.repository.RepoResult
import com.nejracoric.digitalnialbum.data.repository.StickerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AlbumUiState(
    val stickers: List<Sticker> = emptyList(),
    val recentStickers: List<Sticker> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOnline: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.BROJ,
    val filterOwned: FilterOwned = FilterOwned.SVE,
    val selectedTeam: String = "Sve",
    val layout: ListLayout = ListLayout.MREZA,
    val teams: List<String> = listOf("Sve"),
    val collected: Int = 0,
    val total: Int = 0
) {
    val percent: Int get() = if (total == 0) 0 else (collected * 100) / total
}

private data class AlbumFilters(
    val sort: SortOption = SortOption.BROJ,
    val filter: FilterOwned = FilterOwned.SVE,
    val team: String = "Sve",
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val error: String? = null
)

class AlbumViewModel(
    private val repository: StickerRepository,
    private val preferences: UserPreferences,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val filters = MutableStateFlow(AlbumFilters())

    val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow("search", "")

    val uiState: StateFlow<AlbumUiState> = combine(
        combine(
            repository.stickers,
            repository.isOnline,
            repository.ownedCount,
            repository.totalCount,
            preferences.layout
        ) { all, online, owned, total, layout ->
            listOf(all, online, owned, total, layout)
        },
        searchQuery,
        filters
    ) { partial, query, f ->
        @Suppress("UNCHECKED_CAST")
        val all = partial[0] as List<Sticker>
        val online = partial[1] as Boolean
        val owned = partial[2] as Int
        val total = partial[3] as Int
        val layout = partial[4] as ListLayout
        val teams = listOf("Sve") + all.map { it.team }.distinct().sorted()
        val recent = all.filter { it.owned && it.obtainedAt != null }
            .sortedByDescending { it.obtainedAt }
            .take(6)
        val filtered = all
            .filter { s ->
                val q = query.isBlank() ||
                    s.name.contains(query, true) ||
                    s.number.contains(query) ||
                    s.team.contains(query, true)
                val teamOk = f.team == "Sve" || s.team == f.team
                val ownedOk = when (f.filter) {
                    FilterOwned.SVE -> true
                    FilterOwned.SKUPLJENE -> s.owned
                    FilterOwned.NEDOSTAJU -> !s.owned
                }
                q && teamOk && ownedOk
            }
            .let { list ->
                when (f.sort) {
                    SortOption.BROJ -> list.sortedBy { it.number.toIntOrNull() ?: 0 }
                    SortOption.NAZIV -> list.sortedBy { it.name }
                    SortOption.DATUM -> list.sortedByDescending { it.obtainedAt ?: 0L }
                    SortOption.RIJETKOST -> list.sortedBy { it.rarity }
                }
            }
        AlbumUiState(
            stickers = filtered,
            recentStickers = recent,
            isLoading = f.loading,
            isRefreshing = f.refreshing,
            isOnline = online,
            error = f.error,
            searchQuery = query,
            sortOption = f.sort,
            filterOwned = f.filter,
            selectedTeam = f.team,
            layout = layout,
            teams = teams,
            collected = owned,
            total = total
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlbumUiState())

    init {
        viewModelScope.launch {
            preferences.preferredTeam.collect { team ->
                if (filters.value.team == "Sve" && team != "Sve") {
                    filters.update { it.copy(team = team) }
                }
            }
        }
        refresh(initial = true)
    }

    fun onSearchChange(value: String) {
        savedStateHandle["search"] = value
    }

    fun onSortChange(option: SortOption) {
        filters.update { it.copy(sort = option) }
    }

    fun onFilterChange(filter: FilterOwned) {
        filters.update { it.copy(filter = filter) }
    }

    fun onTeamChange(team: String) {
        filters.update { it.copy(team = team) }
    }

    fun refresh(initial: Boolean = false) {
        viewModelScope.launch {
            filters.update {
                it.copy(
                    loading = initial,
                    refreshing = !initial,
                    error = null
                )
            }
            when (val result = repository.refreshFromNetwork()) {
                is RepoResult.Success -> filters.update { it.copy(error = null) }
                is RepoResult.Error -> filters.update { it.copy(error = result.message) }
            }
            filters.update { it.copy(loading = false, refreshing = false) }
        }
    }
}

class AlbumViewModelFactory(
    private val repository: StickerRepository,
    private val preferences: UserPreferences,
    private val savedStateHandle: SavedStateHandle
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AlbumViewModel(repository, preferences, savedStateHandle) as T
    }
}
