package com.nejracoric.digitalnialbum.ui.memory

data class MemoryLevelConfig(
    val level: Int,
    val pairs: Int,
    val timeSeconds: Int,
    val columns: Int
) {
    val cards: Int get() = pairs * 2
}

object MemoryLevels {
    val all = listOf(
        MemoryLevelConfig(1, pairs = 4, timeSeconds = 45, columns = 4),
        MemoryLevelConfig(2, pairs = 6, timeSeconds = 50, columns = 4),
        MemoryLevelConfig(3, pairs = 8, timeSeconds = 55, columns = 4),
        MemoryLevelConfig(4, pairs = 10, timeSeconds = 60, columns = 5),
        MemoryLevelConfig(5, pairs = 12, timeSeconds = 70, columns = 4),
        MemoryLevelConfig(6, pairs = 14, timeSeconds = 80, columns = 4),
        MemoryLevelConfig(7, pairs = 16, timeSeconds = 90, columns = 4),
        MemoryLevelConfig(8, pairs = 18, timeSeconds = 100, columns = 6)
    )

    fun config(level: Int): MemoryLevelConfig =
        all.firstOrNull { it.level == level } ?: all.last()

    /** Bodovi = preostalo vrijeme / 3, min 1 ako pobijediš. */
    fun scoreFor(secondsLeft: Int): Float {
        if (secondsLeft <= 0) return 0f
        return (secondsLeft / 3f).coerceAtLeast(1f)
    }
}

enum class MemoryCardFace { CREST, STICKER }

data class MemoryCard(
    val id: Int,
    val pairKey: String,
    val label: String,
    val imageModel: Any,
    val face: MemoryCardFace
)

data class MemoryTile(
    val card: MemoryCard,
    val faceUp: Boolean = false,
    val matched: Boolean = false
)
