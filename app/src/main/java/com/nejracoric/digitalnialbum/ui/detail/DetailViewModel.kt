package com.nejracoric.digitalnialbum.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nejracoric.digitalnialbum.data.repository.StickerRepository
import com.nejracoric.digitalnialbum.util.resolveCrestUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val sticker: com.nejracoric.digitalnialbum.data.model.Sticker? = null,
    val crestUrl: String? = null,
    val loading: Boolean = true,
    val message: String? = null
)

class DetailViewModel(
    private val repository: StickerRepository,
    private val stickerId: Int
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.stickers, repository.crestUrls) { list, crests ->
                val item = list.find { it.id == stickerId }
                if (item != null) {
                    _state.update {
                        it.copy(
                            sticker = item,
                            crestUrl = resolveCrestUrl(item.team, crests),
                            loading = false,
                            message = null
                        )
                    }
                }
            }.collect { }
        }
        viewModelScope.launch {
            val item = repository.getSticker(stickerId)
            if (item != null) {
                val crests = repository.crestUrls.value
                _state.update {
                    it.copy(
                        sticker = item,
                        crestUrl = resolveCrestUrl(item.team, crests),
                        loading = false,
                        message = null
                    )
                }
            } else if (_state.value.sticker == null) {
                _state.update {
                    it.copy(loading = false, message = "Sličica nije pronađena")
                }
            }
        }
    }

    fun toggleFavorite() {
        val s = _state.value.sticker ?: return
        viewModelScope.launch {
            repository.toggleFavorite(s.id, s.isFavorite)
        }
    }

    fun toggleWishlist() {
        val s = _state.value.sticker ?: return
        viewModelScope.launch {
            repository.toggleWishlist(s.id, s.isWished)
        }
    }
}

class DetailViewModelFactory(
    private val repository: StickerRepository,
    private val stickerId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetailViewModel(repository, stickerId) as T
    }
}
