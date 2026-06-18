package com.nejracoric.digitalnialbum.ui.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nejracoric.digitalnialbum.ui.components.CollectionBarChart
import com.nejracoric.digitalnialbum.ui.components.CollectionRing
import com.nejracoric.digitalnialbum.ui.components.FifaBackground
import com.nejracoric.digitalnialbum.ui.theme.FifaGold
import com.nejracoric.digitalnialbum.ui.theme.FifaGray
import com.nejracoric.digitalnialbum.ui.theme.FifaGreen
import com.nejracoric.digitalnialbum.ui.theme.FifaNavy

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    showBack: Boolean = true,
    onBack: () -> Unit = {},
    onDuplicates: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    FifaBackground {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "STATISTIKA",
                style = MaterialTheme.typography.labelLarge,
                color = FifaGold,
                fontWeight = FontWeight.Bold
            )
            CollectionRing(
                percent = state.percent,
                collected = state.collected,
                total = state.total,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            CollectionBarChart(
                collected = state.collected,
                missing = state.missing,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.duplicateCount > 0) {
                Button(
                    onClick = onDuplicates,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FifaGreen,
                        contentColor = FifaNavy
                    )
                ) {
                    Text("Duplikati (${state.duplicateCount})", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
