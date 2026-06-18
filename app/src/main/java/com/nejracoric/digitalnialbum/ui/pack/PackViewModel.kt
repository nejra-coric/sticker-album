package com.nejracoric.digitalnialbum.ui.pack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.data.repository.RepoResult
import com.nejracoric.digitalnialbum.data.repository.StickerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PackUiState(
    val lastPack: List<Sticker> = emptyList(),
    val opening: Boolean = false,
    val shakeHint: Boolean = true,
    val error: String? = null,
    val isOnline: Boolean = true
)

class PackViewModel(
    private val repository: StickerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PackUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.isOnline.collect { online ->
                _state.update { it.copy(isOnline = online) }
            }
        }
    }

    fun onShakeDetected() {
        if (_state.value.opening || !_state.value.isOnline) return
        openPack()
    }

    fun openPack() {
        viewModelScope.launch {
            _state.update { it.copy(opening = true, error = null, shakeHint = false) }
            when (val result = repository.openPack()) {
                is RepoResult.Success -> {
                    _state.update {
                        it.copy(
                            lastPack = result.data,
                            opening = false,
                            error = null
                        )
                    }
                }
                is RepoResult.Error -> {
                    _state.update {
                        it.copy(
                            opening = false,
                            error = result.message,
                            shakeHint = true
                        )
                    }
                }
            }
        }
    }
}

class PackViewModelFactory(
    private val repository: StickerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PackViewModel(repository) as T
    }
}
