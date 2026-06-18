package com.nejracoric.digitalnialbum

import android.app.Application
import com.nejracoric.digitalnialbum.data.preferences.UserPreferences
import com.nejracoric.digitalnialbum.data.repository.StickerRepository

class DigitalAlbumApp : Application() {
    lateinit var repository: StickerRepository
        private set
    lateinit var preferences: UserPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        repository = StickerRepository(this)
        preferences = UserPreferences(this)
    }
}
