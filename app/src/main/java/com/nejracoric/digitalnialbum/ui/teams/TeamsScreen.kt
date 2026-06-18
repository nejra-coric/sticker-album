package com.nejracoric.digitalnialbum.ui.teams

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nejracoric.digitalnialbum.data.model.TeamProgress
import com.nejracoric.digitalnialbum.ui.components.FifaBackground
import com.nejracoric.digitalnialbum.ui.theme.FifaGold
import com.nejracoric.digitalnialbum.ui.theme.FifaGray
import com.nejracoric.digitalnialbum.ui.theme.FifaGreen
import com.nejracoric.digitalnialbum.ui.theme.FifaNavyCard

@Composable
fun TeamsScreen(
    viewModel: TeamsViewModel,
    onTeamClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    FifaBackground {
        Column(Modifier.fillMaxSize()) {
            Text(
                "REPREZENTACIJE",
                style = MaterialTheme.typography.labelLarge,
                color = FifaGold,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )
            Text(
                "Napredak po timu",
                style = MaterialTheme.typography.bodyMedium,
                color = FifaGray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FifaGreen)
                }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error ?: "")
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.teams, key = { it.code }) { team ->
                        TeamCard(team = team, onClick = { onTeamClick(team.name) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamCard(team: TeamProgress, onClick: () -> Unit) {
    val progress by animateFloatAsState(
        targetValue = team.percent / 100f,
        animationSpec = tween(600),
        label = "teamProgress"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FifaNavyCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = team.crestUrl,
                contentDescription = team.name,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column(Modifier.padding(start = 14.dp).weight(1f)) {
                Text(team.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "${team.collected}/${team.total}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FifaGray
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    color = FifaGreen,
                    trackColor = FifaNavyCard
                )
            }
            Text(
                "${team.percent}%",
                fontWeight = FontWeight.Bold,
                color = FifaGold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
