package com.nejracoric.digitalnialbum.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent

@Composable
fun GoldShimmerOverlay(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "goldShimmer")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )

    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    GoldAccent.copy(0.12f),
                    Color(0xFFFFA500).copy(0.28f),
                    GoldAccent.copy(0.18f),
                    Color.Transparent
                ),
                start = Offset(w * shift, 0f),
                end = Offset(w * (shift + 0.6f), h)
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    GoldAccent.copy(0.15f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.35f),
                radius = w * 0.7f
            )
        )
    }
}
