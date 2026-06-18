package com.nejracoric.digitalnialbum.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.nejracoric.digitalnialbum.ui.theme.AlbumGold
import com.nejracoric.digitalnialbum.ui.theme.AlbumTeal

@Composable
fun CollectionBarChart(
    collected: Int,
    missing: Int,
    modifier: Modifier = Modifier
) {
    val total = (collected + missing).coerceAtLeast(1)
    Column(modifier = modifier.padding(top = 16.dp)) {
        Text("Skupljene vs nedostaju", style = MaterialTheme.typography.titleMedium)
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 12.dp)) {
            val barW = size.width * 0.38f
            val gap = size.width * 0.12f
            val maxH = size.height * 0.9f
            val h1 = maxH * collected / total
            val h2 = maxH * missing / total
            drawRoundRect(
                color = AlbumTeal,
                topLeft = Offset(gap, size.height - h1),
                size = Size(barW, h1),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = AlbumGold,
                topLeft = Offset(gap + barW + gap, size.height - h2),
                size = Size(barW, h2),
                cornerRadius = CornerRadius(10f, 10f)
            )
        }
        Text(
            "Zeleno: moje  ·  Žuto: fale",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
