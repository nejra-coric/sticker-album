package com.nejracoric.digitalnialbum.ui.album

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nejracoric.digitalnialbum.data.model.FilterOwned
import com.nejracoric.digitalnialbum.data.model.SortOption
import com.nejracoric.digitalnialbum.ui.components.GlassBackground
import com.nejracoric.digitalnialbum.ui.components.GlassChip
import com.nejracoric.digitalnialbum.ui.components.PlayerGlassCard
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan
import com.nejracoric.digitalnialbum.ui.theme.TextGray
import com.nejracoric.digitalnialbum.ui.theme.TextWhite
import com.nejracoric.digitalnialbum.util.resolveCrestUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    viewModel: AlbumViewModel,
    onOpenDetail: (Int) -> Unit,
    onFavorites: () -> Unit,
    onSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var sortExpanded by remember { mutableStateOf(false) }

    GlassBackground {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            "GLAVNI ALBUM",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    state.error?.let { msg ->
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                msg,
                                color = Color(0xFFFFB74D),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                    item(span = { GridItemSpan(2) }) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::onSearchChange,
                            placeholder = { Text("Pretraži igrača...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0x4400E5FF),
                                focusedContainerColor = Color(0x221A2340),
                                unfocusedContainerColor = Color(0x221A2340),
                                cursorColor = NeonCyan
                            )
                        )
                    }
                    item(span = { GridItemSpan(2) }) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x221A2340))
                                    .border(1.dp, Color(0x4400E5FF), RoundedCornerShape(16.dp))
                                    .clickable { sortExpanded = true }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Sortiraj po: ${sortLabel(state.sortOption)}",
                                    color = TextWhite,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(Icons.Default.ArrowDropDown, null, tint = NeonCyan)
                            }
                            DropdownMenu(
                                expanded = sortExpanded,
                                onDismissRequest = { sortExpanded = false }
                            ) {
                                SortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(sortLabel(option)) },
                                        onClick = {
                                            viewModel.onSortChange(option)
                                            sortExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item(span = { GridItemSpan(2) }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(
                                listOf(
                                    FilterOwned.SVE to "Sve",
                                    FilterOwned.SKUPLJENE to "Skupljeno",
                                    FilterOwned.DUPLIKATI to "Duplikati"
                                )
                            ) { (filter, label) ->
                                GlassChip(
                                    label = label,
                                    selected = state.filterOwned == filter,
                                    onClick = { viewModel.onFilterChange(filter) }
                                )
                            }
                        }
                    }
                    if (state.stickers.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Nema rezultata", color = TextGray)
                            }
                        }
                    } else {
                        items(state.stickers, key = { it.id }) { sticker ->
                            PlayerGlassCard(
                                sticker = sticker,
                                crestUrl = resolveCrestUrl(sticker.team, state.crestUrls),
                                onClick = { onOpenDetail(sticker.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun sortLabel(sort: SortOption) = when (sort) {
    SortOption.BROJ -> "Broju"
    SortOption.NAZIV -> "Imenu"
    SortOption.DATUM -> "Datumu"
    SortOption.RIJETKOST -> "Rijetkosti"
}
