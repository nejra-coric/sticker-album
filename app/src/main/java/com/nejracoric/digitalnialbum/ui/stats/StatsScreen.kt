package com.nejracoric.digitalnialbum.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nejracoric.digitalnialbum.ui.components.CollectionOverviewChart
import com.nejracoric.digitalnialbum.ui.components.GlassBackground
import com.nejracoric.digitalnialbum.ui.components.GlassCard
import com.nejracoric.digitalnialbum.ui.components.RarityOverviewChart
import com.nejracoric.digitalnialbum.ui.theme.TextWhite

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    showBack: Boolean = true,
    onBack: () -> Unit = {},
    onDuplicates: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    GlassBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "MOJA STATISTIKA",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), corner = 18.dp) {
                    CollectionOverviewChart(
                        collected = state.collected,
                        missing = state.missing,
                        duplicates = state.duplicateExtras,
                        total = state.total,
                        percent = state.percent
                    )
                }
            }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), corner = 18.dp) {
                    RarityOverviewChart(bars = state.rarityBars)
                }
            }
        }
    }
}
