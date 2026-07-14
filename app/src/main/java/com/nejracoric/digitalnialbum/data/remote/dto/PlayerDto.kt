package com.nejracoric.digitalnialbum.data.remote.dto

import com.google.gson.annotations.JsonAdapter
import com.nejracoric.digitalnialbum.data.remote.ApiConfig
import com.nejracoric.digitalnialbum.util.rarityFromDrawChance

data class PlayerDto(
    @JsonAdapter(PlayerIdDeserializer::class)
    val id: Int,
    val ime: String = "",
    val prezime: String = "",
    val broj_dresa: Int = 0,
    val reprezentacija: String = "",
    val pozicija: String = "",
    val slicica_lokacija: String = "",
    val tip_slicice: String? = null,
    val zlatna: Boolean? = null,
    val vjerovatnoca: Int? = null
) {
    fun isPlayerCard(): Boolean {
        val tip = tip_slicice?.lowercase().orEmpty()
        return id > 0 && tip != "grb" && ime.isNotBlank()
    }

    fun fullName() = "$ime $prezime".trim()

    fun imageUrl() = when {
        slicica_lokacija.isNotBlank() -> ApiConfig.imageUrl(slicica_lokacija)
        else -> ApiConfig.stickerImageUrl(id)
    }

    fun rarityLabel() = rarityFromDrawChance(
        playerId = id,
        tipSlicice = tip_slicice,
        zlatna = zlatna,
        vjerovatnoca = vjerovatnoca
    )

    fun isGoldenCard(): Boolean {
        val label = rarityLabel()
        return label.contains("zlat", true) || label.contains("legend", true)
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
