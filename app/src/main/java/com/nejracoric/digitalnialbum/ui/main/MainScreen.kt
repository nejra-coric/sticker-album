package com.nejracoric.digitalnialbum.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.nejracoric.digitalnialbum.DigitalAlbumApp
import com.nejracoric.digitalnialbum.ui.album.AlbumScreen
import com.nejracoric.digitalnialbum.ui.album.AlbumViewModel
import com.nejracoric.digitalnialbum.ui.album.AlbumViewModelFactory
import com.nejracoric.digitalnialbum.ui.components.GlassBackground
import com.nejracoric.digitalnialbum.ui.pack.PackScreen
import com.nejracoric.digitalnialbum.ui.pack.PackViewModel
import com.nejracoric.digitalnialbum.ui.pack.PackViewModelFactory
import com.nejracoric.digitalnialbum.ui.favorites.FavoritesContent
import com.nejracoric.digitalnialbum.ui.stats.StatsScreen
import com.nejracoric.digitalnialbum.ui.stats.StatsViewModel
import com.nejracoric.digitalnialbum.ui.stats.StatsViewModelFactory
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan
import com.nejracoric.digitalnialbum.ui.theme.TextGray
import com.nejracoric.digitalnialbum.ui.settings.SettingsScreen
import androidx.compose.runtime.collectAsState

private data class TabItem(val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem("Album", Icons.Default.Collections),
    TabItem("Paketići", Icons.Default.ShoppingBag),
    TabItem("Statistika", Icons.Default.EmojiEvents),
    TabItem("Favoriti", Icons.Default.Favorite),
    TabItem("Profil", Icons.Default.Person)
)

@Composable
fun MainScreen(
    entry: NavBackStackEntry,
    app: DigitalAlbumApp,
    onOpenDetail: (Int) -> Unit,
    onFavorites: () -> Unit,
    onSettings: () -> Unit,
    onDuplicates: () -> Unit,
    onMemory: () -> Unit,
    onTrade: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val albumVm: AlbumViewModel = viewModel(
        viewModelStoreOwner = entry,
        factory = AlbumViewModelFactory(app.repository, app.preferences, entry.savedStateHandle)
    )

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (selectedTab == index) GoldAccent else TextGray
                                )
                            },
                            label = {
                                Text(
                                    tab.label,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedTab == index) GoldAccent else TextGray
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GoldAccent,
                                selectedTextColor = GoldAccent,
                                unselectedIconColor = TextGray,
                                unselectedTextColor = TextGray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                    label = "tabs"
                ) { tab ->
                    when (tab) {
                        0 -> AlbumScreen(
                            viewModel = albumVm,
                            onOpenDetail = onOpenDetail,
                            onFavorites = onFavorites,
                            onSettings = onSettings
                        )
                        1 -> {
                            val packVm: PackViewModel = viewModel(
                                viewModelStoreOwner = entry,
                                key = "pack_vm",
                                factory = PackViewModelFactory(app.repository, app.preferences)
                            )
                            PackScreen(
                                viewModel = packVm,
                                onOpenDetail = onOpenDetail
                            )
                        }
                        2 -> {
                            val statsVm: StatsViewModel = viewModel(
                                viewModelStoreOwner = entry,
                                key = "stats_vm",
                                factory = StatsViewModelFactory(app.repository)
                            )
                            StatsScreen(viewModel = statsVm, onDuplicates = onDuplicates)
                        }
                        3 -> {
                            val favs by app.repository.favorites.collectAsState(initial = emptyList())
                            FavoritesContent(
                                list = favs,
                                onOpenDetail = onOpenDetail,
                                showTitle = true
                            )
                        }
                        4 -> SettingsScreen(
                            onBack = {},
                            showBack = false,
                            title = "Profil",
                            onMemory = onMemory,
                            onTrade = onTrade
                        )
                    }
                }
            }
        }
    }
}
