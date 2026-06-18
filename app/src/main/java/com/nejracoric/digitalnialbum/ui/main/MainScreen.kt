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
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.nejracoric.digitalnialbum.DigitalAlbumApp
import com.nejracoric.digitalnialbum.ui.album.AlbumScreen
import com.nejracoric.digitalnialbum.ui.album.AlbumViewModel
import com.nejracoric.digitalnialbum.ui.album.AlbumViewModelFactory
import com.nejracoric.digitalnialbum.ui.components.FifaBackground
import com.nejracoric.digitalnialbum.ui.pack.PackScreen
import com.nejracoric.digitalnialbum.ui.pack.PackViewModel
import com.nejracoric.digitalnialbum.ui.pack.PackViewModelFactory
import com.nejracoric.digitalnialbum.ui.stats.StatsScreen
import com.nejracoric.digitalnialbum.ui.stats.StatsViewModel
import com.nejracoric.digitalnialbum.ui.stats.StatsViewModelFactory
import com.nejracoric.digitalnialbum.ui.teams.TeamsScreen
import com.nejracoric.digitalnialbum.ui.teams.TeamsViewModel
import com.nejracoric.digitalnialbum.ui.teams.TeamsViewModelFactory
import com.nejracoric.digitalnialbum.ui.theme.FifaGold
import com.nejracoric.digitalnialbum.ui.theme.FifaGreen
import com.nejracoric.digitalnialbum.ui.theme.FifaNavyCard
import com.nejracoric.digitalnialbum.ui.theme.FifaNavyLight
import com.nejracoric.digitalnialbum.ui.wishlist.WishlistScreen

private data class TabItem(val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem("Album", Icons.Default.Collections),
    TabItem("Timovi", Icons.Default.Flag),
    TabItem("Paketić", Icons.Default.ShoppingBag),
    TabItem("Želje", Icons.Default.Star),
    TabItem("Stats", Icons.Default.EmojiEvents)
)

@Composable
fun MainScreen(
    entry: NavBackStackEntry,
    app: DigitalAlbumApp,
    onOpenDetail: (Int) -> Unit,
    onFavorites: () -> Unit,
    onSettings: () -> Unit,
    onDuplicates: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var pendingTeam by rememberSaveable { mutableStateOf<String?>(null) }

    val albumVm: AlbumViewModel = viewModel(
        viewModelStoreOwner = entry,
        factory = AlbumViewModelFactory(app.repository, app.preferences, entry.savedStateHandle)
    )

    LaunchedEffect(pendingTeam) {
        pendingTeam?.let { team ->
            albumVm.onTeamChange(team)
            selectedTab = 0
            pendingTeam = null
        }
    }

    FifaBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = FifaNavyLight,
                    contentColor = Color.White
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (selectedTab == index) FifaGreen else Color(0xFF8E99A8)
                                )
                            },
                            label = {
                                Text(
                                    tab.label,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = FifaGreen,
                                selectedTextColor = FifaGreen,
                                unselectedIconColor = Color(0xFF8E99A8),
                                unselectedTextColor = Color(0xFF8E99A8),
                                indicatorColor = FifaNavyCard
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
                            val teamsVm: TeamsViewModel = viewModel(
                                viewModelStoreOwner = entry,
                                key = "teams_vm",
                                factory = TeamsViewModelFactory(app.repository)
                            )
                            TeamsScreen(viewModel = teamsVm, onTeamClick = { pendingTeam = it })
                        }
                        2 -> {
                            val packVm: PackViewModel = viewModel(
                                viewModelStoreOwner = entry,
                                key = "pack_vm",
                                factory = PackViewModelFactory(app.repository)
                            )
                            PackScreen(viewModel = packVm, showBack = false)
                        }
                        3 -> WishlistScreen(onOpenDetail = onOpenDetail)
                        4 -> {
                            val statsVm: StatsViewModel = viewModel(
                                viewModelStoreOwner = entry,
                                key = "stats_vm",
                                factory = StatsViewModelFactory(app.repository)
                            )
                            StatsScreen(
                                viewModel = statsVm,
                                showBack = false,
                                onDuplicates = onDuplicates
                            )
                        }
                    }
                }
            }
        }
    }
}
