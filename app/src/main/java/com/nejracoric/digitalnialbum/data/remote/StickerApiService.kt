package com.nejracoric.digitalnialbum.data.remote

import com.nejracoric.digitalnialbum.data.remote.dto.PlayerDto
import com.nejracoric.digitalnialbum.data.remote.dto.TeamDto
import retrofit2.http.GET
import retrofit2.http.Path

interface StickerApiService {
    @GET("api/all-players")
    suspend fun getAllPlayers(): List<PlayerDto>

    @GET("api/random-players-unique/{count}")
    suspend fun getRandomPack(@Path("count") count: Int): List<PlayerDto>

    @GET("api/grbovi")
    suspend fun getTeams(): List<TeamDto>
}
