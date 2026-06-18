package com.nejracoric.digitalnialbum.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nejracoric.digitalnialbum.data.preferences.UserPreferences
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val preferences: UserPreferences
) : ViewModel() {

    fun finish(language: String) {
        viewModelScope.launch {
            preferences.setLanguage(language)
            preferences.setOnboardingDone()
        }
    }
}

class OnboardingViewModelFactory(
    private val preferences: UserPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OnboardingViewModel(preferences) as T
    }
}
