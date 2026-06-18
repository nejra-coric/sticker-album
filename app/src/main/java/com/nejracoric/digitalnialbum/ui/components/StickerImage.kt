package com.nejracoric.digitalnialbum.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nejracoric.digitalnialbum.ui.theme.FifaGold
import com.nejracoric.digitalnialbum.ui.theme.FifaNavyCard

@Composable
fun StickerImage(
    url: String,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    owned: Boolean = true,
    isGolden: Boolean = false
) {
    val shape = RoundedCornerShape(12.dp)
    val mod = if (size != null) modifier.size(size) else modifier
    val borderMod = when {
        isGolden -> Modifier.border(
            2.5.dp,
            Brush.linearGradient(listOf(FifaGold, Color(0xFFFFA500), FifaGold)),
            shape
        )
        else -> Modifier
    }

    Box(
        mod
            .clip(shape)
            .then(borderMod)
            .alpha(if (owned) 1f else 0.35f)
    ) {
        if (url.isBlank()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(FifaNavyCard),
                contentAlignment = Alignment.Center
            ) {
                Text(name.take(2).uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            AsyncImage(
                model = url,
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        if (!owned) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )
        }
    }
}

@Composable
fun StickerImageLarge(
    url: String,
    name: String,
    modifier: Modifier = Modifier,
    isGolden: Boolean = false
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        StickerImage(
            url = url,
            name = name,
            modifier = Modifier.fillMaxSize(),
            isGolden = isGolden
        )
    }
}
