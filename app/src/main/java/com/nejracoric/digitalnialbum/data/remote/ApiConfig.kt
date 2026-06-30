package com.nejracoric.digitalnialbum.data.remote

object ApiConfig {
    const val BASE_URL = "http://49.13.125.189:3300/"

    fun imageUrl(path: String): String {
        if (path.startsWith("http")) return path
        val normalized = if (path.startsWith("/")) path else "/$path"
        return BASE_URL.trimEnd('/') + normalized
    }

    fun stickerImageUrl(playerId: Int): String = imageUrl("/api/slicica/$playerId")

    fun crestImageUrl(teamCode: String): String = imageUrl("/api/grb/$teamCode")
}
