package com.nejracoric.digitalnialbum.ui.trade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.data.preferences.Economy
import com.nejracoric.digitalnialbum.ui.components.GlassBackground
import com.nejracoric.digitalnialbum.ui.components.GlassCard
import com.nejracoric.digitalnialbum.ui.components.StickerImage
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan
import com.nejracoric.digitalnialbum.ui.theme.TextGray
import com.nejracoric.digitalnialbum.ui.theme.TextWhite
import com.nejracoric.digitalnialbum.util.effectiveImageUrl

@Composable
fun TradeScreen(
    viewModel: TradeViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val points by viewModel.points.collectAsState()
    val selected = state.selectedMissing
    val byId = (state.missing + state.duplicates).associateBy { it.id }

    GlassBackground {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (selected != null) viewModel.clearSelection() else onBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextWhite)
                }
                Text(
                    if (selected == null) "TRADE" else "Ponuđači",
                    color = NeonCyan,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${TradeViewModel.format(points)} pts",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            state.message?.let {
                Text(
                    it,
                    color = GoldAccent,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (selected == null) {
                Text(
                    "Nedostaje ti · tapni za trade (dupe → poeni ${Economy.TRADE_HALF_POINTS}–${Economy.TRADE_FULL_POINTS.toInt()})",
                    color = TextGray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "Duplikati: ${state.duplicates.size} · Nedostaje: ${state.missing.size}",
                    color = NeonCyan,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.missing, key = { it.id }) { sticker ->
                        MissingRow(sticker) { viewModel.selectMissing(sticker) }
                    }
                }
            } else {
                Text(
                    "Tražiš: ${selected.name}",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.traders, key = { it.id }) { trader ->
                        val wants = byId[trader.wantsDuplicateId]
                        GlassCard(modifier = Modifier.fillMaxWidth(), corner = 14.dp) {
                            Column(Modifier.padding(14.dp)) {
                                Text(trader.name, color = GoldAccent, fontWeight = FontWeight.Bold)
                                Text(
                                    "Nudi tvoju nedostajuću · Traži: ${wants?.name ?: "#${trader.wantsDuplicateId}"}",
                                    color = TextGray,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    if (trader.fairTrade) "+1.0 poen" else "+0.5 poena",
                                    color = NeonCyan,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                GlassCard(
                                    corner = 10.dp,
                                    golden = true,
                                    onClick = { viewModel.executeTrade(trader) }
                                ) {
                                    Text(
                                        "Zamijeni",
                                        Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissingRow(sticker: Sticker, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        corner = 12.dp,
        onClick = onClick
    ) {
        Row(
            Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StickerImage(
                url = sticker.effectiveImageUrl(),
                name = sticker.name,
                stickerId = sticker.id,
                size = 48.dp,
                owned = false
            )
            Column(Modifier.padding(start = 12.dp)) {
                Text(sticker.name, color = TextWhite, fontWeight = FontWeight.SemiBold)
                Text(
                    "${sticker.team} · #${sticker.number}",
                    color = TextGray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
