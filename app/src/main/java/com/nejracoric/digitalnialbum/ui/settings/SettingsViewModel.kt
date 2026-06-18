package com.nejracoric.digitalnialbum.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nejracoric.digitalnialbum.data.model.ListLayout
import com.nejracoric.digitalnialbum.data.preferences.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: UserPreferences
) : ViewModel() {

    val layout = preferences.layout.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ListLayout.LISTA
    )

    val team = preferences.preferredTeam.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "Sve"
    )

    fun setLayout(value: ListLayout) {
        viewModelScope.launch { preferences.setLayout(value) }
    }

    fun setTeam(value: String) {
        viewModelScope.launch { preferences.setFavoriteTeam(value) }
    }
}

class SettingsViewModelFactory(
    private val preferences: UserPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(preferences) as T
    }
}
