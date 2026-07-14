package com.nejracoric.digitalnialbum.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nejracoric.digitalnialbum.DigitalAlbumApp
import com.nejracoric.digitalnialbum.ui.detail.DetailScreen
import com.nejracoric.digitalnialbum.ui.detail.DetailViewModel
import com.nejracoric.digitalnialbum.ui.detail.DetailViewModelFactory
import com.nejracoric.digitalnialbum.ui.duplicates.DuplicatesScreen
import com.nejracoric.digitalnialbum.ui.favorites.FavoritesScreen
import com.nejracoric.digitalnialbum.ui.main.MainScreen
import com.nejracoric.digitalnialbum.ui.onboarding.OnboardingScreen
import com.nejracoric.digitalnialbum.ui.memory.MemoryScreen
import com.nejracoric.digitalnialbum.ui.memory.MemoryViewModel
import com.nejracoric.digitalnialbum.ui.memory.MemoryViewModelFactory
import com.nejracoric.digitalnialbum.ui.trade.TradeScreen
import com.nejracoric.digitalnialbum.ui.trade.TradeViewModel
import com.nejracoric.digitalnialbum.ui.trade.TradeViewModelFactory
import com.nejracoric.digitalnialbum.ui.settings.SettingsScreen
import com.nejracoric.digitalnialbum.ui.splash.SplashScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as DigitalAlbumApp
    val onboardingDone by app.preferences.onboardingCompleted.collectAsState(initial = false)

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onboardingDone = onboardingDone,
                onFinished = { goOnboarding ->
                    if (goOnboarding) {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onDone = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.MAIN) { entry ->
            MainScreen(
                entry = entry,
                app = app,
                onOpenDetail = { id ->
                    navController.navigate(Routes.detail(id)) {
                        launchSingleTop = true
                    }
                },
                onFavorites = { navController.navigate(Routes.FAVORITES) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onDuplicates = { navController.navigate(Routes.DUPLICATES) },
                onMemory = { navController.navigate(Routes.MEMORY) },
                onTrade = { navController.navigate(Routes.TRADE) }
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("stickerId") { type = NavType.IntType })
        ) { entry ->
            val stickerId = entry.arguments?.getInt("stickerId") ?: return@composable
            val vm: DetailViewModel = viewModel(
                viewModelStoreOwner = entry,
                factory = DetailViewModelFactory(app.repository, stickerId)
            )
            DetailScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.FAVORITES) {
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onOpenDetail = { id ->
                    navController.navigate(Routes.detail(id)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DUPLICATES) {
            DuplicatesScreen(
                onBack = { navController.popBackStack() },
                onOpenDetail = { id ->
                    navController.navigate(Routes.detail(id)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.MEMORY) {
            val vm: MemoryViewModel = viewModel(factory = MemoryViewModelFactory(app))
            MemoryScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.TRADE) {
            val vm: TradeViewModel = viewModel(factory = TradeViewModelFactory(app))
            TradeScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}
