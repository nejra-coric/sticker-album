package com.nejracoric.digitalnialbum

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.nejracoric.digitalnialbum.data.preferences.UserPreferences
import com.nejracoric.digitalnialbum.data.repository.StickerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DigitalAlbumApp : Application(), ImageLoaderFactory {
    lateinit var repository: StickerRepository
        private set
    lateinit var preferences: UserPreferences
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        repository = StickerRepository(this)
        preferences = UserPreferences(this)
        appScope.launch { repository.prefetchCachedImages() }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_disk_cache"))
                    .maxSizeBytes(80L * 1024 * 1024)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }
}
