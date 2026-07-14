package com.nejracoric.digitalnialbum.ui.pack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.data.preferences.Economy
import com.nejracoric.digitalnialbum.data.preferences.UserPreferences
import com.nejracoric.digitalnialbum.data.repository.RepoResult
import com.nejracoric.digitalnialbum.data.repository.StickerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PackPhase { FOIL, CARDS }

data class PackUiState(
    val phase: PackPhase = PackPhase.FOIL,
    val lastPack: List<Sticker> = emptyList(),
    val opening: Boolean = false,
    val requestFreshPack: Boolean = false,
    val error: String? = null,
    val isOnline: Boolean = true,
    val crestUrls: Map<String, String> = emptyMap()
)

class PackViewModel(
    private val repository: StickerRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(PackUiState())
    val state = _state.asStateFlow()

    val points = preferences.points.stateIn(viewModelScope, SharingStarted.Eagerly, 0f)
    val freePacksUsed = preferences.freePacksUsed.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        viewModelScope.launch {
            repository.isOnline.collect { online ->
                _state.update { it.copy(isOnline = online) }
            }
        }
        viewModelScope.launch {
            repository.crestUrls.collect { crests ->
                _state.update { it.copy(crestUrls = crests) }
            }
        }
        viewModelScope.launch {
            val last = repository.getLastOpenedPack()
            if (last.isNotEmpty()) {
                _state.update {
                    it.copy(
                        lastPack = last,
                        phase = PackPhase.CARDS,
                        requestFreshPack = false
                    )
                }
            }
        }
    }

    /** Vrati se na foil; sljedeći shake/tap vuče NOVI paketić s API-ja. */
    fun prepareNewPack() {
        if (_state.value.opening) return
        _state.update {
            it.copy(
                phase = PackPhase.FOIL,
                requestFreshPack = true,
                error = null
            )
        }
    }

    fun onShakeDetected() {
        if (_state.value.phase != PackPhase.FOIL) return
        if (_state.value.opening) return
        revealOrOpenPack()
    }

    fun revealOrOpenPack() {
        if (_state.value.opening) return
        val wantsFresh = _state.value.requestFreshPack
        viewModelScope.launch {
            if (!wantsFresh) {
                val last = repository.getLastOpenedPack()
                if (last.isNotEmpty()) {
                    _state.update {
                        it.copy(
                            lastPack = last,
                            phase = PackPhase.CARDS,
                            opening = false,
                            error = null,
                            requestFreshPack = false
                        )
                    }
                    return@launch
                }
            }

            if (!_state.value.isOnline) {
                _state.update {
                    it.copy(error = "Potreban internet za novi paketić.")
                }
                return@launch
            }

            val used = freePacksUsed.value
            val pts = points.value
            if (used >= Economy.FREE_PACKS) {
                if (pts < Economy.PACK_COST) {
                    _state.update {
                        it.copy(
                            error = "Nemaš dovoljno poena (${Economy.PACK_COST.toInt()} potreban). Igraj Memory ili Trade!"
                        )
                    }
                    return@launch
                }
            }

            _state.update { it.copy(opening = true, error = null, phase = PackPhase.FOIL) }
            when (val result = repository.openPack()) {
                is RepoResult.Success -> {
                    if (!preferences.consumeFreePack()) {
                        preferences.trySpendPoints(Economy.PACK_COST)
                    }
                    _state.update {
                        it.copy(
                            lastPack = result.data,
                            phase = PackPhase.CARDS,
                            opening = false,
                            requestFreshPack = false,
                            error = null
                        )
                    }
                }
                is RepoResult.Error -> {
                    _state.update {
                        it.copy(
                            opening = false,
                            phase = PackPhase.FOIL,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
}

class PackViewModelFactory(
    private val repository: StickerRepository,
    private val preferences: UserPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PackViewModel(repository, preferences) as T
    }
}
