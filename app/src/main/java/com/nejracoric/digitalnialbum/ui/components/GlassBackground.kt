package com.nejracoric.digitalnialbum.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.nejracoric.digitalnialbum.ui.theme.DarkBlueBackground
import com.nejracoric.digitalnialbum.ui.theme.DarkBlueMid
import com.nejracoric.digitalnialbum.ui.theme.MagentaAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan

@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DarkBlueBackground, DarkBlueMid, Color(0xFF0F1630))
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(NeonCyan.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.15f),
                    radius = size.width * 0.55f
                ),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.2f, size.height * 0.15f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(MagentaAccent.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.75f),
                    radius = size.width * 0.45f
                ),
                radius = size.width * 0.45f,
                center = Offset(size.width * 0.85f, size.height * 0.75f)
            )
        }
        content()
    }
}

@Composable
fun GlassPackBackground(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A2A1F), DarkBlueBackground, DarkBlueMid)
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(NeonCyan.copy(alpha = 0.2f), Color.Transparent),
                    radius = size.width * 0.7f
                ),
                radius = size.width * 0.7f,
                center = Offset(size.width / 2f, size.height * 0.35f)
            )
        }
        content()
    }
}
