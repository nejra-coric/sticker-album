package com.nejracoric.digitalnialbum.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
    const val DETAIL = "detail/{stickerId}"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val DUPLICATES = "duplicates"
    const val MEMORY = "memory"
    const val TRADE = "trade"

    fun detail(id: Int) = "detail/$id"
}
