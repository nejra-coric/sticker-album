package com.nejracoric.digitalnialbum.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun ScratchReveal(
    modifier: Modifier = Modifier,
    revealed: Boolean,
    onRevealed: () -> Unit,
    content: @Composable () -> Unit
) {
    val strokes = remember { mutableStateListOf<Path>() }
    var dragPoints by remember { mutableIntStateOf(0) }

    Box(modifier) {
        content()
        if (!revealed) {
            Canvas(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val path = Path().apply { moveTo(offset.x, offset.y) }
                                strokes.add(path)
                            },
                            onDrag = { change, _ ->
                                if (strokes.isNotEmpty()) {
                                    strokes[strokes.lastIndex].lineTo(
                                        change.position.x,
                                        change.position.y
                                    )
                                }
                                dragPoints++
                                change.consume()
                                if (dragPoints > 25) onRevealed()
                            }
                        )
                    }
            ) {
                drawRect(Color(0xFFB0BEC5))
                drawRect(Color(0xFF78909C).copy(alpha = 0.5f))
                strokes.forEach { path ->
                    drawPath(
                        path = path,
                        color = Color.Transparent,
                        style = Stroke(width = 40f, cap = StrokeCap.Round),
                        blendMode = BlendMode.Clear
                    )
                }
            }
        }
    }
}
