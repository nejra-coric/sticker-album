package com.nejracoric.digitalnialbum.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.MagentaAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan
import com.nejracoric.digitalnialbum.ui.theme.TextGray
import com.nejracoric.digitalnialbum.ui.theme.TextWhite

data class ChartSlice(val label: String, val value: Int, val color: Color)

private val ChartTeal = Color(0xFF4DB6AC)
private val ChartGold = Color(0xFFE9C46A)
private val ChartPurple = Color(0xFF9575CD)

@Composable
fun CollectionOverviewChart(
    collected: Int,
    missing: Int,
    duplicates: Int,
    total: Int,
    percent: Int,
    modifier: Modifier = Modifier
) {
    val slices = listOf(
        ChartSlice("Skupljeno", collected, ChartTeal),
        ChartSlice("Nedostaje", missing, ChartGold),
        ChartSlice("Duplikati", duplicates, ChartPurple)
    )
    val sum = slices.sumOf { it.value }.coerceAtLeast(1)

    Column(modifier.padding(16.dp)) {
        Text(
            "Pregled kolekcije",
            color = TextGray,
            style = MaterialTheme.typography.labelMedium
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(150.dp)) {
                    val stroke = 22f
                    var start = -90f
                    slices.forEach { slice ->
                        val sweep = 360f * slice.value / sum
                        drawArc(
                            color = slice.color,
                            startAngle = start,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(stroke / 2, stroke / 2),
                            size = Size(size.width - stroke, size.height - stroke),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        start += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$percent%",
                        color = TextWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Skupljeno",
                        color = TextGray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                slices.forEach { slice ->
                    val pct = slice.value * 100 / sum
                    LegendItem(
                        color = slice.color,
                        text = "$pct% ${slice.label}"
                    )
                }
            }
        }
        Text(
            "Ukupno sličica: $total",
            color = TextGray,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text,
            color = TextWhite,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun RarityOverviewChart(
    bars: List<ChartSlice>,
    modifier: Modifier = Modifier
) {
    val max = bars.maxOfOrNull { it.value }?.coerceAtLeast(1) ?: 1
    val yMax = ((max + 49) / 50) * 50
    val gridSteps = 4

    Column(modifier.padding(16.dp)) {
        Text(
            "Po rijetkosti",
            color = TextGray,
            style = MaterialTheme.typography.labelMedium
        )
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(190.dp)
                .padding(top = 12.dp)
        ) {
            val leftPad = 40f
            val bottomPad = 24f
            val chartW = size.width - leftPad - 12f
            val chartH = size.height - bottomPad

            for (i in 0..gridSteps) {
                val y = chartH - (chartH * i / gridSteps)
                val value = yMax * i / gridSteps
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(leftPad, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#8B95B0")
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                    drawText(
                        value.toString(),
                        leftPad - 8f,
                        y + 10f,
                        paint
                    )
                }
            }

            val barW = chartW / (bars.size * 2.2f)
            bars.forEachIndexed { i, bar ->
                val h = chartH * bar.value / yMax
                val left = leftPad + barW * 0.6f + i * (barW * 1.8f)
                drawRoundRect(
                    color = bar.color,
                    topLeft = Offset(left, chartH - h),
                    size = Size(barW, h),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#8B95B0")
                        textSize = 26f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText(
                        bar.label.uppercase(),
                        left + barW / 2f,
                        size.height - 4f,
                        paint
                    )
                }
            }
        }
    }
}

fun collectionDuplicateExtras(stickers: List<Sticker>): Int =
    stickers.sumOf { if (it.ownedCount > 1) it.ownedCount - 1 else 0 }

fun rarityBars(stickers: List<Sticker>): List<ChartSlice> {
    val owned = stickers.filter { it.owned }
    val groups = owned.groupBy { sticker ->
        when {
            sticker.rarity.contains("legend", true) -> "LEGEND"
            sticker.rarity.contains("zlat", true) || sticker.isGolden -> "GOLD"
            sticker.rarity.contains("rijed", true) -> "RARE"
            else -> "COMMON"
        }
    }
    return listOf(
        ChartSlice("COMMON", groups["COMMON"]?.size ?: 0, ChartTeal),
        ChartSlice("RARE", groups["RARE"]?.size ?: 0, ChartGold),
        ChartSlice("GOLD", groups["GOLD"]?.size ?: 0, ChartGold),
        ChartSlice("LEGEND", groups["LEGEND"]?.size ?: 0, ChartPurple)
    )
}
