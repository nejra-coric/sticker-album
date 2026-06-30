package com.nejracoric.digitalnialbum.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.nejracoric.digitalnialbum.util.ImageCache
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan

@Composable
fun StickerImage(
    url: String,
    name: String,
    modifier: Modifier = Modifier,
    stickerId: Int? = null,
    size: Dp? = null,
    owned: Boolean = true,
    isGolden: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(12.dp)
    val mod = if (size != null) modifier.size(size) else modifier
    val borderMod = when {
        isGolden -> Modifier.border(
            2.5.dp,
            Brush.linearGradient(listOf(GoldAccent, Color(0xFFFFA500), GoldAccent)),
            shape
        )
        else -> Modifier
    }

    Box(
        mod
            .clip(shape)
            .then(borderMod)
            .alpha(if (owned) 1f else 0.78f)
    ) {
        val model = ImageCache.resolveSticker(context, stickerId, url)
        if (stickerId == null && url.isBlank()) {
            PlaceholderBox(name)
        } else {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(model)
                    .diskCacheKey(stickerId?.let { "sticker-$it" } ?: url)
                    .memoryCacheKey(stickerId?.let { "sticker-$it" } ?: url)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NeonCyan,
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = { PlaceholderBox(name) }
            )
        }
        if (!owned) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.12f))
            )
        }
    }
}

@Composable
fun StickerImageLarge(
    url: String,
    name: String,
    modifier: Modifier = Modifier,
    stickerId: Int? = null,
    isGolden: Boolean = false
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        StickerImage(
            url = url,
            name = name,
            stickerId = stickerId,
            modifier = Modifier.fillMaxSize(),
            isGolden = isGolden,
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun CrestImage(
    url: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    val model = ImageCache.resolveCrest(context, url) ?: return
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(model)
            .diskCacheKey(url)
            .memoryCacheKey(url)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        loading = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = NeonCyan,
                    strokeWidth = 1.5.dp
                )
            }
        }
    )
}

@Composable
private fun PlaceholderBox(name: String) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF1A2340)),
        contentAlignment = Alignment.Center
    ) {
        Text(name.take(2).uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
