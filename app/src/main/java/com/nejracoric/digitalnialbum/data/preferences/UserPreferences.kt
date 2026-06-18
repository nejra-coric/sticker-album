package com.nejracoric.digitalnialbum.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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
}
