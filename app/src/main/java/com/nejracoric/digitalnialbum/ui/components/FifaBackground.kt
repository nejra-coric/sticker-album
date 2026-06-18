package com.nejracoric.digitalnialbum.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.nejracoric.digitalnialbum.ui.theme.FifaGreen
import com.nejracoric.digitalnialbum.ui.theme.FifaNavy
import com.nejracoric.digitalnialbum.ui.theme.FifaNavyLight
import com.nejracoric.digitalnialbum.ui.theme.FifaPurple

@Composable
fun FifaBackground(
    modifier: Modifier = Modifier,
    accent: Color = FifaNavy,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(accent, FifaNavy, FifaNavyLight),
                    startY = 0f,
                    endY = 1800f
                )
            )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            FifaPurple.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(900f, 200f),
                        radius = 700f
                    )
                )
        )
        content()
    }
}

@Composable
fun FifaPackBackground(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A2E1A), Color(0xFF0A0E21), FifaNavy)
                )
            )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(FifaGreen.copy(alpha = 0.18f), Color.Transparent),
                        radius = 900f
                    )
                )
        )
        content()
    }
}
