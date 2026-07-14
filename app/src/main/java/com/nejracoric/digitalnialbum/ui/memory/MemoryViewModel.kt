package com.nejracoric.digitalnialbum.ui.memory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nejracoric.digitalnialbum.DigitalAlbumApp
import com.nejracoric.digitalnialbum.data.preferences.UserPreferences
import com.nejracoric.digitalnialbum.data.repository.StickerRepository
import com.nejracoric.digitalnialbum.util.ImageCache
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MemoryPhase { MENU, PLAYING, WON, LOST }

data class MemoryUiState(
    val phase: MemoryPhase = MemoryPhase.MENU,
    val level: Int = 1,
    val unlockedLevel: Int = 1,
    val tiles: List<MemoryTile> = emptyList(),
    val columns: Int = 4,
    val secondsLeft: Int = 0,
    val timeLimit: Int = 0,
    val pointsEarned: Float = 0f,
    val flippedIds: List<Int> = emptyList(),
    val busy: Boolean = false,
    val message: String? = null,
    val availableImages: Int = 0
)

class MemoryViewModel(
    app: Application,
    private val repository: StickerRepository,
    private val preferences: UserPreferences
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(MemoryUiState())
    val state = _state.asStateFlow()

    val points = preferences.points.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        0f
    )

    private var timerJob: Job? = null
    private var tileIdSeq = 0

    init {
        viewModelScope.launch {
            preferences.memoryLevelUnlocked.collect { unlocked ->
                _state.update { it.copy(unlockedLevel = unlocked) }
            }
        }
        viewModelScope.launch { refreshAvailableCount() }
    }

    private suspend fun refreshAvailableCount() {
        val ctx = getApplication<Application>()
        val stickers = ImageCache.listCachedStickerIds(ctx).size
        val crests = ImageCache.listCachedCrestFiles(ctx).size
        val catalog = repository.stickers.first().size
        _state.update { it.copy(availableImages = maxOf(stickers + crests, catalog)) }
    }

    fun startLevel(level: Int) {
        viewModelScope.launch {
            if (level > _state.value.unlockedLevel) {
                _state.update { it.copy(message = "Level $level još nije otključan.") }
                return@launch
            }
            val config = MemoryLevels.config(level)
            val deck = buildDeck(config.pairs)
            if (deck == null) {
                _state.update {
                    it.copy(
                        message = "Nema dovoljno slika za ovaj level. Otvori paketiće ili osvježi album."
                    )
                }
                return@launch
            }
            timerJob?.cancel()
            _state.update {
                it.copy(
                    phase = MemoryPhase.PLAYING,
                    level = level,
                    tiles = deck.map { card -> MemoryTile(card) },
                    columns = config.columns,
                    secondsLeft = config.timeSeconds,
                    timeLimit = config.timeSeconds,
                    pointsEarned = 0f,
                    flippedIds = emptyList(),
                    busy = false,
                    message = null
                )
            }
            timerJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    if (_state.value.phase != MemoryPhase.PLAYING) break
                    val left = _state.value.secondsLeft - 1
                    if (left <= 0) {
                        _state.update {
                            it.copy(
                                secondsLeft = 0,
                                phase = MemoryPhase.LOST,
                                message = "Vrijeme isteklo!"
                            )
                        }
                        break
                    }
                    _state.update { it.copy(secondsLeft = left) }
                }
            }
        }
    }

    fun onTileClick(tileId: Int) {
        val s = _state.value
        if (s.phase != MemoryPhase.PLAYING || s.busy) return
        val tile = s.tiles.find { it.card.id == tileId } ?: return
        if (tile.faceUp || tile.matched) return
        if (s.flippedIds.size >= 2) return

        val flipped = s.flippedIds + tileId
        _state.update {
            it.copy(
                tiles = it.tiles.map { t ->
                    if (t.card.id == tileId) t.copy(faceUp = true) else t
                },
                flippedIds = flipped
            )
        }

        if (flipped.size < 2) return

        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            delay(550)
            val a = _state.value.tiles.find { it.card.id == flipped[0] }
            val b = _state.value.tiles.find { it.card.id == flipped[1] }
            val match = a != null && b != null && a.card.pairKey == b.card.pairKey
            if (match) {
                _state.update { st ->
                    st.copy(
                        tiles = st.tiles.map { t ->
                            if (t.card.id in flipped) t.copy(matched = true, faceUp = true) else t
                        },
                        flippedIds = emptyList(),
                        busy = false
                    )
                }
                checkWin()
            } else {
                _state.update { st ->
                    st.copy(
                        tiles = st.tiles.map { t ->
                            if (t.card.id in flipped) t.copy(faceUp = false) else t
                        },
                        flippedIds = emptyList(),
                        busy = false
                    )
                }
            }
        }
    }

    private fun checkWin() {
        val s = _state.value
        if (s.tiles.isEmpty() || s.tiles.any { !it.matched }) return
        timerJob?.cancel()
        val score = MemoryLevels.scoreFor(s.secondsLeft)
        viewModelScope.launch {
            preferences.addPoints(score)
            preferences.unlockMemoryLevel(s.level + 1)
            _state.update {
                it.copy(
                    phase = MemoryPhase.WON,
                    pointsEarned = score,
                    message = "Level ${s.level} završen! +${formatPoints(score)} poena"
                )
            }
        }
    }

    fun backToMenu() {
        timerJob?.cancel()
        _state.update {
            it.copy(
                phase = MemoryPhase.MENU,
                tiles = emptyList(),
                flippedIds = emptyList(),
                busy = false,
                message = null
            )
        }
        viewModelScope.launch { refreshAvailableCount() }
    }

    private suspend fun buildDeck(pairs: Int): List<MemoryCard>? {
        val ctx = getApplication<Application>()
        val sources = linkedMapOf<String, Any>()

        ImageCache.listCachedStickerIds(ctx).shuffled().forEach { id ->
            sources["s$id"] = ImageCache.stickerFile(ctx, id)
        }
        ImageCache.listCachedCrestFiles(ctx).shuffled().forEach { file ->
            sources["c${file.name}"] = file
        }

        if (sources.size < pairs) {
            repository.stickers.first().shuffled().forEach { sticker ->
                if (sources.size >= pairs) return@forEach
                val key = "s${sticker.id}"
                if (!sources.containsKey(key)) {
                    sources[key] = ImageCache.resolveSticker(ctx, sticker.id, sticker.imageUrl)
                }
            }
        }

        if (sources.size < pairs) {
            repository.crestUrls.value.values.distinct().forEach { url ->
                if (sources.size >= pairs) return@forEach
                val key = "u${url.hashCode()}"
                if (!sources.containsKey(key)) {
                    sources[key] = ImageCache.resolveCrest(ctx, url) ?: url
                }
            }
        }

        if (sources.size < pairs) return null

        tileIdSeq = 0
        return sources.entries.take(pairs).flatMap { (key, model) ->
            val face = if (key.startsWith("s")) MemoryCardFace.STICKER else MemoryCardFace.CREST
            listOf(
                MemoryCard(++tileIdSeq, key, key, model, face),
                MemoryCard(++tileIdSeq, key, key, model, face)
            )
        }.shuffled()
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun formatPoints(p: Float): String =
            if (p == p.toLong().toFloat()) p.toLong().toString() else "%.1f".format(p)
    }
}

class MemoryViewModelFactory(
    private val app: DigitalAlbumApp
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MemoryViewModel(app, app.repository, app.preferences) as T
    }
}
