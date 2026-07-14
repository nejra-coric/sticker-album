package com.nejracoric.digitalnialbum.ui.memory

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nejracoric.digitalnialbum.ui.components.GlassBackground
import com.nejracoric.digitalnialbum.ui.components.GlassCard
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan
import com.nejracoric.digitalnialbum.ui.theme.TextGray
import com.nejracoric.digitalnialbum.ui.theme.TextWhite

@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val points by viewModel.points.collectAsState()

    GlassBackground {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (state.phase == MemoryPhase.PLAYING) viewModel.backToMenu()
                    else onBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextWhite)
                }
                Text(
                    "MEMORY",
                    color = NeonCyan,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${MemoryViewModel.formatPoints(points)} pts",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            when (state.phase) {
                MemoryPhase.MENU -> MemoryMenu(
                    unlocked = state.unlockedLevel,
                    available = state.availableImages,
                    message = state.message,
                    onStart = viewModel::startLevel
                )
                MemoryPhase.PLAYING -> MemoryBoard(
                    state = state,
                    onTile = viewModel::onTileClick
                )
                MemoryPhase.WON, MemoryPhase.LOST -> MemoryResult(
                    won = state.phase == MemoryPhase.WON,
                    level = state.level,
                    pointsEarned = state.pointsEarned,
                    message = state.message,
                    onMenu = viewModel::backToMenu,
                    onRetry = { viewModel.startLevel(state.level) },
                    onNext = {
                        viewModel.startLevel(state.level + 1)
                    },
                    canNext = state.phase == MemoryPhase.WON &&
                        state.level < MemoryLevels.all.last().level
                )
            }
        }
    }
}

@Composable
private fun MemoryMenu(
    unlocked: Int,
    available: Int,
    message: String?,
    onStart: (Int) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Pronađi parove sličica i grbova iz keša. Brže = više poena.",
            color = TextGray,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Dostupne slike: $available",
            color = NeonCyan,
            style = MaterialTheme.typography.labelMedium
        )
        message?.let {
            Text(it, color = Color(0xFFFFB74D), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(8.dp))
        MemoryLevels.all.forEach { cfg ->
            val locked = cfg.level > unlocked
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                corner = 14.dp,
                golden = !locked && cfg.level == unlocked,
                onClick = if (locked) null else ({ onStart(cfg.level) })
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Level ${cfg.level}",
                            color = if (locked) TextGray else TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${cfg.pairs} parova · ${cfg.timeSeconds}s",
                            color = TextGray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                        Text(
                            if (locked) "ZAKLJUČANO" else "IGRAJ",
                            color = if (locked) TextGray else GoldAccent,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                }
            }
        }
    }
}

@Composable
private fun MemoryBoard(
    state: MemoryUiState,
    onTile: (Int) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Level ${state.level}", color = TextWhite, fontWeight = FontWeight.Bold)
            Text(
                "${state.secondsLeft}s",
                color = if (state.secondsLeft <= 10) Color(0xFFFF6B6B) else GoldAccent,
                fontWeight = FontWeight.Black
            )
        }
        LinearProgressIndicator(
            progress = {
                if (state.timeLimit == 0) 0f
                else state.secondsLeft / state.timeLimit.toFloat()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            color = NeonCyan,
            trackColor = Color(0x33445577)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(state.columns),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.tiles, key = { it.card.id }) { tile ->
                MemoryTileCard(tile = tile, onClick = { onTile(tile.card.id) })
            }
        }
    }
}

@Composable
private fun MemoryTileCard(tile: MemoryTile, onClick: () -> Unit) {
    val revealed = tile.faceUp || tile.matched
    val rot by animateFloatAsState(if (revealed) 180f else 0f, label = "flip")
    Box(
        Modifier
            .aspectRatio(0.75f)
            .graphicsLayer {
                rotationY = rot
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(12.dp))
            .background(if (tile.matched) Color(0xFF1A3A2A) else Color(0xFF121A33))
            .border(
                1.5.dp,
                if (tile.matched) GoldAccent else NeonCyan.copy(0.5f),
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !tile.matched && !tile.faceUp, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (revealed) {
            AsyncImage(
                model = tile.card.imageModel,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                    .graphicsLayer { rotationY = 180f },
                contentScale = ContentScale.Fit
            )
        } else {
            Text("?", color = NeonCyan, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun MemoryResult(
    won: Boolean,
    level: Int,
    pointsEarned: Float,
    message: String?,
    onMenu: () -> Unit,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    canNext: Boolean
) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (won) "POBJEDA!" else "KRAJ",
            color = if (won) GoldAccent else Color(0xFFFF6B6B),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black
        )
        message?.let {
            Text(
                it,
                color = TextWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
        if (won) {
            Text(
                "+${MemoryViewModel.formatPoints(pointsEarned)} poena",
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(24.dp))
        GlassCard(corner = 14.dp, onClick = onRetry) {
            Text(
                "Ponovi level $level",
                Modifier.padding(16.dp),
                color = TextWhite,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (canNext) {
            Spacer(Modifier.height(10.dp))
            GlassCard(corner = 14.dp, golden = true, onClick = onNext) {
                Text(
                    "Sljedeći level",
                    Modifier.padding(16.dp),
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        GlassCard(corner = 14.dp, onClick = onMenu) {
            Text(
                "Natrag na meni",
                Modifier.padding(16.dp),
                color = TextGray
            )
        }
    }
}
