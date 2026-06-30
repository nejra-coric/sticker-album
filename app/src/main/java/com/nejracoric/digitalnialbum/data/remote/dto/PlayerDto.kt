package com.nejracoric.digitalnialbum.data.remote.dto

import com.nejracoric.digitalnialbum.data.remote.ApiConfig

data class PlayerDto(
    val id: Int,
    val ime: String,
    val prezime: String,
    val broj_dresa: Int,
    val reprezentacija: String,
    val pozicija: String,
    val slicica_lokacija: String,
    val tip_slicice: String? = null,
    val zlatna: Boolean? = null
) {
    fun fullName() = "$ime $prezime"

    fun imageUrl() = when {
        slicica_lokacija.isNotBlank() -> ApiConfig.imageUrl(slicica_lokacija)
        else -> ApiConfig.stickerImageUrl(id)
    }

    fun rarityLabel() = when (tip_slicice?.lowercase()) {
        "legendarna" -> "Legendarna"
        "rijedka" -> "Rijetka"
        "zlatna" -> "Zlatna"
        else -> if (zlatna == true) "Zlatna" else "Obična"
    }
}

data class TeamDto(
    val id: String,
    val reprezentacija: String,
    val slicica_lokacija: String
) {
    fun crestUrl() = ApiConfig.imageUrl(slicica_lokacija)
    fun code() = id.removePrefix("grb-")
}
