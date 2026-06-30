package com.nejracoric.digitalnialbum.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GlassScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DarkBlueBackground,
    primaryContainer = Color(0xFF0D2840),
    secondary = GoldAccent,
    onSecondary = DarkBlueBackground,
    tertiary = MagentaAccent,
    background = DarkBlueBackground,
    onBackground = TextWhite,
    surface = DarkBlueSurface,
    onSurface = TextWhite,
    surfaceVariant = Color(0xFF1A2340),
    onSurfaceVariant = TextGray,
    outline = GlassBorder
)

@Composable
fun DigitalniAlbumTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GlassScheme,
        typography = Typography,
        content = content
    )
}
