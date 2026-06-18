package com.nejracoric.digitalnialbum.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nejracoric.digitalnialbum.data.local.dao.StickerDao
import com.nejracoric.digitalnialbum.data.local.entity.FavoriteEntity
import com.nejracoric.digitalnialbum.data.local.entity.OwnedStickerEntity
import com.nejracoric.digitalnialbum.data.local.entity.StickerEntity
import com.nejracoric.digitalnialbum.data.local.entity.WishlistEntity

@Database(
    entities = [
        StickerEntity::class,
        OwnedStickerEntity::class,
        FavoriteEntity::class,
        WishlistEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stickerDao(): StickerDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "digitalni_album.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
