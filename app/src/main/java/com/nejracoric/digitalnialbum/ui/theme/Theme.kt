package com.nejracoric.digitalnialbum.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FifaColorScheme = darkColorScheme(
    primary = FifaGreen,
    onPrimary = FifaNavy,
    primaryContainer = Color(0xFF0D3D2B),
    onPrimaryContainer = FifaGreen,
    secondary = FifaGold,
    onSecondary = FifaNavy,
    secondaryContainer = Color(0xFF3D3200),
    onSecondaryContainer = FifaGold,
    tertiary = FifaCyan,
    onTertiary = FifaNavy,
    background = FifaNavy,
    onBackground = FifaWhite,
    surface = FifaNavyLight,
    onSurface = FifaWhite,
    surfaceVariant = FifaNavyCard,
    onSurfaceVariant = FifaGray,
    outline = Color(0xFF2E3A5C),
    error = Color(0xFFFF5252)
)

@Composable
fun DigitalniAlbumTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FifaColorScheme,
        typography = Typography,
        content = content
    )
}
