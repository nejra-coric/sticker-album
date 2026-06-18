package com.nejracoric.digitalnialbum.data.remote

object ApiConfig {
    const val BASE_URL = "http://49.13.125.189:3300/"

    fun imageUrl(path: String): String {
        if (path.startsWith("http")) return path
        return BASE_URL.trimEnd('/') + path
    }
}
