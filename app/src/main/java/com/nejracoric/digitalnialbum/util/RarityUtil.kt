package com.nejracoric.digitalnialbum.util

import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.data.remote.ApiConfig
import com.nejracoric.digitalnialbum.data.remote.dto.TeamDto

enum class RarityTier(val sortOrder: Int) {
    LEGEND(0),
    GOLD(1),
    RARE(2),
    COMMON(3)
}

fun Sticker.rarityTier(): RarityTier = rarityTierFrom(rarity, isGolden)

fun rarityTierFrom(rarity: String, isGolden: Boolean = false): RarityTier {
    val r = rarity.lowercase()
    return when {
        "legend" in r -> RarityTier.LEGEND
        "zlat" in r || "gold" in r || isGolden -> RarityTier.GOLD
        "rijed" in r || "rare" in r -> RarityTier.RARE
        else -> RarityTier.COMMON
    }
}

fun Sticker.rarityLabel(): String = when (rarityTier()) {
    RarityTier.LEGEND -> "LEGEND"
    RarityTier.GOLD -> "GOLD"
    RarityTier.RARE -> "RARE"
    RarityTier.COMMON -> "COMMON"
}

fun Sticker.displayRating(): Int = when (rarityTier()) {
    RarityTier.LEGEND -> 91
    RarityTier.GOLD -> 88
    RarityTier.RARE -> 82
    RarityTier.COMMON -> 75
}

fun Sticker.raritySortKey(): Int = rarityTier().sortOrder

fun rarityRank(rarity: String, isGolden: Boolean = false): Int = when (rarityTierFrom(rarity, isGolden)) {
    RarityTier.LEGEND -> 3
    RarityTier.GOLD -> 2
    RarityTier.RARE -> 1
    RarityTier.COMMON -> 0
}

/**
 * Rijetkost po API šansi (vjerovatnoca) i tip_slicice.
 * Katalog (/api/all-players) nema tip — koristi stabilan roll po id-u
 * usklađen s tipičnim drop rateovima (većina COMMON, manje RARE/GOLD/LEGEND).
 *
 * API primjeri: vjerovatnoca=100 → obična, vjerovatnoca=20 → zlatna.
 */
fun rarityFromDrawChance(
    playerId: Int,
    tipSlicice: String? = null,
    zlatna: Boolean? = null,
    vjerovatnoca: Int? = null
): String {
    when (tipSlicice?.lowercase()) {
        "legendarna", "legend" -> return "Legendarna"
        "rijedka", "rare" -> return "Rijetka"
        "zlatna", "gold" -> return "Zlatna"
    }
    if (zlatna == true) return "Zlatna"

    vjerovatnoca?.let { chance ->
        return when {
            chance <= 5 -> "Legendarna"
            chance <= 20 -> "Zlatna"
            chance <= 50 -> "Rijetka"
            else -> "Obična"
        }
    }

    // Stabilna šansa za katalog (isti igrač → isti raritet)
    val roll = ((playerId * 37) % 100).let { if (it < 0) it + 100 else it }
    return when {
        roll < 3 -> "Legendarna"   // ~3%
        roll < 12 -> "Zlatna"      // ~9%
        roll < 32 -> "Rijetka"     // ~20%
        else -> "Obična"
    }
}

fun Sticker.effectiveImageUrl(): String =
    imageUrl.ifBlank { ApiConfig.stickerImageUrl(id) }

private val TEAM_NAME_TO_CODE = mapOf(
    "bosna i hercegovina" to "BIH",
    "bosna" to "BIH",
    "bih" to "BIH",
    "hrvatska" to "CRO",
    "croatia" to "CRO",
    "cro" to "CRO",
    "srbija" to "SRB",
    "serbia" to "SRB",
    "njemačka" to "GER",
    "germany" to "GER",
    "ger" to "GER",
    "francuska" to "FRA",
    "france" to "FRA",
    "fra" to "FRA",
    "engleska" to "ENG",
    "england" to "ENG",
    "eng" to "ENG",
    "brazil" to "BRA",
    "brazilija" to "BRA",
    "bra" to "BRA",
    "argentina" to "ARG",
    "arg" to "ARG",
    "portugal" to "POR",
    "por" to "POR",
    "holandija" to "NED",
    "nizozemska" to "NED",
    "netherlands" to "NED",
    "ned" to "NED",
    "norveška" to "NOR",
    "norveska" to "NOR",
    "norway" to "NOR",
    "nor" to "NOR",
    "belgija" to "BEL",
    "belgium" to "BEL",
    "bel" to "BEL",
    "austrija" to "AUT",
    "austria" to "AUT",
    "aut" to "AUT",
    "turska" to "TUR",
    "turkey" to "TUR",
    "tur" to "TUR",
    "egipat" to "EGY",
    "egypt" to "EGY",
    "egy" to "EGY",
    "maroko" to "MAR",
    "morocco" to "MAR",
    "mar" to "MAR"
)

fun teamCodeFromName(team: String): String {
    val trimmed = team.trim()
    if (trimmed.length == 3 && trimmed.all { it.isLetter() }) {
        return trimmed.uppercase()
    }
    return TEAM_NAME_TO_CODE[trimmed.lowercase()] ?: trimmed.uppercase().take(3)
}

fun buildCrestMap(teams: List<TeamDto>): Map<String, String> {
    val map = mutableMapOf<String, String>()
    teams.forEach { team ->
        val url = team.crestUrl()
        map[team.reprezentacija] = url
        map[team.reprezentacija.uppercase()] = url
        val code = team.code().uppercase()
        map[code] = url
        map[team.reprezentacija.lowercase()] = url
    }
    return map
}

fun resolveCrestUrl(team: String, crestMap: Map<String, String>): String {
    crestMap[team]?.let { return it }
    crestMap[team.uppercase()]?.let { return it }
    crestMap[team.lowercase()]?.let { return it }
    crestMap.entries.firstOrNull { (key, _) ->
        key.equals(team, true) ||
            team.contains(key, true) ||
            key.contains(team, true)
    }?.value?.let { return it }
    val code = teamCodeFromName(team)
    return crestMap[code] ?: ApiConfig.crestImageUrl(code)
}
