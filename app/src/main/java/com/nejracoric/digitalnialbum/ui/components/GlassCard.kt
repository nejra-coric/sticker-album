package com.nejracoric.digitalnialbum.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nejracoric.digitalnialbum.ui.theme.GlassBorder
import com.nejracoric.digitalnialbum.ui.theme.GlassFill
import com.nejracoric.digitalnialbum.ui.theme.GlassGoldBorder
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 16.dp,
    golden: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(corner)
    val borderBrush = if (golden) {
        Brush.linearGradient(listOf(GoldAccent, GlassGoldBorder, GoldAccent))
    } else {
        Brush.linearGradient(listOf(NeonCyan.copy(0.7f), GlassBorder, NeonCyan.copy(0.4f)))
    }
    var mod = modifier
        .clip(shape)
        .background(
            Brush.verticalGradient(
                listOf(GlassFill, Color(0x18FFFFFF), Color(0x0DFFFFFF))
            )
        )
        .border(1.2.dp, borderBrush, shape)
    if (onClick != null) mod = mod.clickable(onClick = onClick)
    Box(mod, content = content)
}
