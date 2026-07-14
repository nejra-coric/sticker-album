package com.nejracoric.digitalnialbum.util

import android.content.Context
import com.nejracoric.digitalnialbum.data.remote.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

object ImageCache {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val semaphore = Semaphore(3)

    fun stickerFile(context: Context, stickerId: Int): File =
        File(context.filesDir, "cache/stickers/$stickerId.png")

    fun crestFile(context: Context, url: String): File {
        val code = url.substringAfterLast('/').ifBlank { url.hashCode().toString() }
        return File(context.filesDir, "cache/crests/$code.png")
    }

    fun listCachedStickerIds(context: Context): List<Int> {
        val dir = File(context.filesDir, "cache/stickers")
        if (!dir.exists()) return emptyList()
        return dir.listFiles()
            ?.mapNotNull { f ->
                f.nameWithoutExtension.toIntOrNull()?.takeIf { f.length() > 0 }
            }
            ?.sorted()
            .orEmpty()
    }

    fun listCachedCrestFiles(context: Context): List<File> {
        val dir = File(context.filesDir, "cache/crests")
        if (!dir.exists()) return emptyList()
        return dir.listFiles()?.filter { it.length() > 0 }.orEmpty()
    }

    fun resolveSticker(context: Context, stickerId: Int?, remoteUrl: String): Any {
        if (stickerId != null) {
            val local = stickerFile(context, stickerId)
            if (local.exists() && local.length() > 0) return local
        }
        return remoteUrl.ifBlank {
            stickerId?.let { ApiConfig.stickerImageUrl(it) } ?: remoteUrl
        }
    }

    fun resolveCrest(context: Context, url: String?): Any? {
        if (url.isNullOrBlank()) return null
        val local = crestFile(context, url)
        if (local.exists() && local.length() > 0) return local
        return url
    }

    suspend fun prefetchSticker(stickerId: Int, url: String, context: Context): Boolean {
        val file = stickerFile(context, stickerId)
        if (file.exists() && file.length() > 0) return true
        val targetUrl = url.ifBlank { ApiConfig.stickerImageUrl(stickerId) }
        return download(targetUrl, file)
    }

    suspend fun prefetchCrest(url: String, context: Context): Boolean {
        val file = crestFile(context, url)
        if (file.exists() && file.length() > 0) return true
        return download(url, file)
    }

    suspend fun prefetchStickers(
        context: Context,
        items: List<Pair<Int, String>>
    ) = withContext(Dispatchers.IO) {
        items.forEach { (id, url) ->
            semaphore.withPermit { prefetchSticker(id, url, context) }
        }
    }

    suspend fun prefetchCrests(context: Context, urls: Collection<String>) =
        withContext(Dispatchers.IO) {
            urls.distinct().forEach { url ->
                semaphore.withPermit { prefetchCrest(url, context) }
            }
        }

    private suspend fun download(url: String, file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            file.parentFile?.mkdirs()
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext false
                val body = response.body ?: return@withContext false
                body.byteStream().use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
