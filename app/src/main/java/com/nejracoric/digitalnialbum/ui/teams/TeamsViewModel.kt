package com.nejracoric.digitalnialbum.ui.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nejracoric.digitalnialbum.data.model.TeamProgress
import com.nejracoric.digitalnialbum.data.repository.StickerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeamsUiState(
    val teams: List<TeamProgress> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class TeamsViewModel(
    private val repository: StickerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TeamsUiState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val teams = repository.getTeamProgress()
                _state.update { it.copy(teams = teams, loading = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.message ?: "Greška")
                }
            }
        }
    }
}

class TeamsViewModelFactory(
    private val repository: StickerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TeamsViewModel(repository) as T
    }
}
