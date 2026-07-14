package com.nejracoric.digitalnialbum.ui.trade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nejracoric.digitalnialbum.DigitalAlbumApp
import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.data.preferences.Economy
import com.nejracoric.digitalnialbum.data.preferences.UserPreferences
import com.nejracoric.digitalnialbum.data.repository.StickerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class FakeTrader(
    val id: Int,
    val name: String,
    val offersMissingId: Int,
    val wantsDuplicateId: Int,
    val fairTrade: Boolean
)

data class TradeUiState(
    val missing: List<Sticker> = emptyList(),
    val duplicates: List<Sticker> = emptyList(),
    val selectedMissing: Sticker? = null,
    val traders: List<FakeTrader> = emptyList(),
    val message: String? = null,
    val loading: Boolean = true
)

class TradeViewModel(
    private val repository: StickerRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(TradeUiState())
    val state = _state.asStateFlow()

    val points = preferences.points.stateIn(viewModelScope, SharingStarted.Eagerly, 0f)

    private val names = listOf(
        "Amina K.", "Marko P.", "Sara N.", "Emir H.", "Lana B.",
        "Tarik M.", "Ena S.", "Nikola V.", "Lejla D.", "Filip J.",
        "Maja R.", "Armin C.", "Ivana T.", "Kenan Ž.", "Petra L."
    )

    init {
        viewModelScope.launch {
            repository.stickers.collect { all ->
                _state.update {
                    it.copy(
                        missing = all.filter { s -> !s.owned }.sortedBy { s -> s.name },
                        duplicates = all.filter { s -> s.ownedCount > 1 }.sortedBy { s -> s.name },
                        loading = false
                    )
                }
            }
        }
    }

    fun selectMissing(sticker: Sticker) {
        val dupes = _state.value.duplicates
        if (dupes.isEmpty()) {
            _state.update {
                it.copy(
                    selectedMissing = sticker,
                    traders = emptyList(),
                    message = "Nemaš duplikata za zamjenu. Otvori paketiće!"
                )
            }
            return
        }
        val traders = List(3) { i ->
            val wants = dupes.random()
            FakeTrader(
                id = i + sticker.id * 10,
                name = names.random(),
                offersMissingId = sticker.id,
                wantsDuplicateId = wants.id,
                fairTrade = Random.nextBoolean()
            )
        }
        _state.update {
            it.copy(
                selectedMissing = sticker,
                traders = traders,
                message = null
            )
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedMissing = null, traders = emptyList(), message = null) }
    }

    fun executeTrade(trader: FakeTrader) {
        viewModelScope.launch {
            val reward = if (trader.fairTrade) Economy.TRADE_FULL_POINTS else Economy.TRADE_HALF_POINTS
            // Simulacija: "dobiješ" nedostajuću tako što je upišemo kao owned (bez gubitka dupla za jednostavnost MVP
            // — ili skinemo jednu owned kopiju). Zatraži repository trade helper.
            val ok = repository.applyTrade(
                receiveId = trader.offersMissingId,
                giveDuplicateId = trader.wantsDuplicateId
            )
            if (!ok) {
                _state.update { it.copy(message = "Trade nije uspio. Provjeri duplikate.") }
                return@launch
            }
            preferences.addPoints(reward)
            _state.update {
                it.copy(
                    selectedMissing = null,
                    traders = emptyList(),
                    message = "Trade uspješan! +${format(reward)} poena"
                )
            }
        }
    }

    companion object {
        fun format(p: Float): String =
            if (p == p.toLong().toFloat()) p.toLong().toString() else "%.1f".format(p)
    }
}

class TradeViewModelFactory(
    private val app: DigitalAlbumApp
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TradeViewModel(app.repository, app.preferences) as T
    }
}
