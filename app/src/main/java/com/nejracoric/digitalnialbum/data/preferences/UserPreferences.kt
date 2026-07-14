package com.nejracoric.digitalnialbum.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nejracoric.digitalnialbum.data.model.ListLayout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class UserPreferences(private val context: Context) {
    private val onboardingDone = booleanPreferencesKey("onboarding_done")
    private val favoriteTeam = stringPreferencesKey("favorite_team")
    private val listLayout = stringPreferencesKey("list_layout")
    private val language = stringPreferencesKey("language")
    private val pointsKey = floatPreferencesKey("album_points")
    private val freePacksUsedKey = intPreferencesKey("free_packs_used")
    private val memoryLevelUnlockedKey = intPreferencesKey("memory_level_unlocked")

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map {
        it[onboardingDone] ?: false
    }

    val preferredTeam: Flow<String> = context.dataStore.data.map {
        it[favoriteTeam] ?: "Sve"
    }

    val layout: Flow<ListLayout> = context.dataStore.data.map {
        if (it[listLayout] == ListLayout.LISTA.name) ListLayout.LISTA else ListLayout.MREZA
    }

    val appLanguage: Flow<String> = context.dataStore.data.map {
        it[language] ?: "bs"
    }

    val points: Flow<Float> = context.dataStore.data.map {
        it[pointsKey] ?: 0f
    }

    val freePacksUsed: Flow<Int> = context.dataStore.data.map {
        it[freePacksUsedKey] ?: 0
    }

    /** Najviši otključani level (1-based). */
    val memoryLevelUnlocked: Flow<Int> = context.dataStore.data.map {
        (it[memoryLevelUnlockedKey] ?: 1).coerceAtLeast(1)
    }

    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[onboardingDone] = true }
    }

    suspend fun setFavoriteTeam(value: String) {
        context.dataStore.edit { it[favoriteTeam] = value }
    }

    suspend fun setLayout(value: ListLayout) {
        context.dataStore.edit { it[listLayout] = value.name }
    }

    suspend fun setLanguage(value: String) {
        context.dataStore.edit { it[language] = value }
    }

    suspend fun addPoints(amount: Float) {
        if (amount == 0f) return
        context.dataStore.edit {
            val current = it[pointsKey] ?: 0f
            it[pointsKey] = (current + amount).coerceAtLeast(0f)
        }
    }

    suspend fun trySpendPoints(amount: Float): Boolean {
        var ok = false
        context.dataStore.edit {
            val current = it[pointsKey] ?: 0f
            if (current >= amount) {
                it[pointsKey] = current - amount
                ok = true
            }
        }
        return ok
    }

    suspend fun consumeFreePack(): Boolean {
        var consumed = false
        context.dataStore.edit {
            val used = it[freePacksUsedKey] ?: 0
            if (used < Economy.FREE_PACKS) {
                it[freePacksUsedKey] = used + 1
                consumed = true
            }
        }
        return consumed
    }

    suspend fun unlockMemoryLevel(level: Int) {
        context.dataStore.edit {
            val current = it[memoryLevelUnlockedKey] ?: 1
            if (level > current) it[memoryLevelUnlockedKey] = level
        }
    }
}

object Economy {
    const val FREE_PACKS = 2
    const val PACK_COST = 5f
    const val TRADE_FULL_POINTS = 1f
    const val TRADE_HALF_POINTS = 0.5f
}
