package com.nejracoric.digitalnialbum.ui.album

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nejracoric.digitalnialbum.data.model.FilterOwned
import com.nejracoric.digitalnialbum.data.model.ListLayout
import com.nejracoric.digitalnialbum.data.model.SortOption
import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.ui.components.CollectionRing
import com.nejracoric.digitalnialbum.ui.components.FifaBackground
import com.nejracoric.digitalnialbum.ui.components.FifaChip
import com.nejracoric.digitalnialbum.ui.components.StickerImage
import com.nejracoric.digitalnialbum.ui.theme.FifaGold
import com.nejracoric.digitalnialbum.ui.theme.FifaGray
import com.nejracoric.digitalnialbum.ui.theme.FifaGreen
import com.nejracoric.digitalnialbum.ui.theme.FifaNavyCard
import com.nejracoric.digitalnialbum.ui.theme.FifaNavyLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    viewModel: AlbumViewModel,
    onOpenDetail: (Int) -> Unit,
    onFavorites: () -> Unit,
    onSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    FifaBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "ALBUM COLLECTION",
                            style = MaterialTheme.typography.labelLarge,
                            color = FifaGold
                        )
                    },
                    actions = {
                        IconButton(onClick = onFavorites) {
                            Icon(Icons.Default.Favorite, null, tint = FifaGold)
                        }
                        IconButton(onClick = onSettings) {
                            Icon(Icons.Default.Settings, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        actionIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            if (state.isLoading) {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FifaGreen)
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            CollectionRing(
                                percent = state.percent,
                                collected = state.collected,
                                total = state.total,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                        if (!state.isOnline) {
                            item {
                                Text(
                                    "Offline režim",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                        state.error?.let { err ->
                            item {
                                Text(
                                    err,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                        if (state.recentStickers.isNotEmpty()) {
                            item {
                                Text(
                                    "NAJNOVIJE",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = FifaGray,
                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                                )
                            }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(state.recentStickers, key = { it.id }) { s ->
                                        RecentSticker(s) { onOpenDetail(s.id) }
                                    }
                                }
                            }
                        }
                        item {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = viewModel::onSearchChange,
                                placeholder = { Text("Pretraži igrača...") },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FifaGreen,
                                    unfocusedBorderColor = Color(0xFF2E3A5C),
                                    focusedContainerColor = FifaNavyCard,
                                    unfocusedContainerColor = FifaNavyCard,
                                    cursorColor = FifaGreen
                                )
                            )
                        }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(FilterOwned.entries.toList()) { f ->
                                    FifaChip(
                                        label = when (f) {
                                            FilterOwned.SVE -> "Sve"
                                            FilterOwned.SKUPLJENE -> "Moje"
                                            FilterOwned.NEDOSTAJU -> "Fale"
                                        },
                                        selected = state.filterOwned == f,
                                        onClick = { viewModel.onFilterChange(f) }
                                    )
                                }
                            }
                        }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    listOf(
                                        SortOption.BROJ to "Dres",
                                        SortOption.NAZIV to "Ime",
                                        SortOption.RIJETKOST to "Rijetkost"
                                    )
                                ) { (opt, label) ->
                                    FifaChip(
                                        label = label,
                                        selected = state.sortOption == opt,
                                        onClick = { viewModel.onSortChange(opt) }
                                    )
                                }
                            }
                        }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.teams.take(8)) { team ->
                                    FifaChip(
                                        label = team,
                                        selected = state.selectedTeam == team,
                                        onClick = { viewModel.onTeamChange(team) }
                                    )
                                }
                            }
                        }
                        item {
                            Text(
                                "SLIČICE (${state.stickers.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = FifaGray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        if (state.stickers.isEmpty()) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().height(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Nema rezultata", color = FifaGray)
                                }
                            }
                        } else if (state.layout == ListLayout.MREZA) {
                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(gridHeight(state.stickers.size)),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    userScrollEnabled = false
                                ) {
                                    items(state.stickers, key = { it.id }) { sticker ->
                                        StickerGridItem(sticker) { onOpenDetail(sticker.id) }
                                    }
                                }
                            }
                        } else {
                            items(state.stickers, key = { it.id }) { sticker ->
                                StickerListItem(
                                    sticker = sticker,
                                    onClick = { onOpenDetail(sticker.id) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun gridHeight(count: Int): androidx.compose.ui.unit.Dp {
    val rows = (count + 2) / 3
    return (rows * 148).dp
}

@Composable
private fun RecentSticker(sticker: Sticker, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FifaNavyCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            StickerImage(
                url = sticker.imageUrl,
                name = sticker.name,
                size = 72.dp,
                isGolden = sticker.isGolden
            )
            Text(
                sticker.name.split(" ").lastOrNull() ?: sticker.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun StickerListItem(sticker: Sticker, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FifaNavyCard),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            StickerImage(
                url = sticker.imageUrl,
                name = sticker.name,
                size = 64.dp,
                owned = sticker.owned,
                isGolden = sticker.isGolden
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(sticker.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text("${sticker.team} · #${sticker.number}", color = FifaGray, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                if (sticker.owned) "✓" else "?",
                color = if (sticker.owned) FifaGreen else FifaGray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StickerGridItem(sticker: Sticker, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (sticker.owned) FifaNavyCard else FifaNavyLight
        ),
        elevation = CardDefaults.cardElevation(if (sticker.isGolden) 8.dp else 2.dp)
    ) {
        Column(
            Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StickerImage(
                url = sticker.imageUrl,
                name = sticker.name,
                size = 90.dp,
                owned = sticker.owned,
                isGolden = sticker.isGolden
            )
            Text(
                sticker.name.split(" ").lastOrNull() ?: "",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text("#${sticker.number}", style = MaterialTheme.typography.labelSmall, color = FifaGray)
        }
    }
}
