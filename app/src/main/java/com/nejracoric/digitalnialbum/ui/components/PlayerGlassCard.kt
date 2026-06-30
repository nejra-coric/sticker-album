package com.nejracoric.digitalnialbum.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.ui.theme.DarkBlueMid
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan
import com.nejracoric.digitalnialbum.ui.theme.TextGray
import com.nejracoric.digitalnialbum.ui.theme.TextWhite
import com.nejracoric.digitalnialbum.util.RarityTier
import com.nejracoric.digitalnialbum.util.displayRating
import com.nejracoric.digitalnialbum.util.effectiveImageUrl
import com.nejracoric.digitalnialbum.util.rarityLabel
import com.nejracoric.digitalnialbum.util.rarityTier

private val CardShape = RoundedCornerShape(22.dp)
private val ImageShape = RoundedCornerShape(18.dp)

@Composable
fun PlayerGlassCard(
    sticker: Sticker,
    onClick: () -> Unit,
    crestUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val tier = sticker.rarityTier()
    val golden = tier == RarityTier.LEGEND || tier == RarityTier.GOLD

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        corner = 22.dp,
        golden = golden,
        onClick = onClick
    ) {
        Column(Modifier.padding(8.dp)) {
            Box(Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "${sticker.displayRating()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = when (tier) {
                            RarityTier.LEGEND, RarityTier.GOLD -> GoldAccent
                            RarityTier.COMMON -> NeonCyan
                        }
                    )
                    Text(
                        sticker.rarityLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (tier) {
                            RarityTier.LEGEND, RarityTier.GOLD -> GoldAccent
                            RarityTier.COMMON -> TextGray
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                if (sticker.owned) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2ECC71)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(vertical = 6.dp)
                    .clip(ImageShape)
                    .background(cardBackground(tier))
            ) {
                StickerImage(
                    url = sticker.effectiveImageUrl(),
                    name = sticker.name,
                    stickerId = sticker.id,
                    modifier = Modifier.fillMaxSize(),
                    owned = sticker.owned,
                    isGolden = tier != RarityTier.COMMON,
                    contentScale = ContentScale.Fit
                )
                when (tier) {
                    RarityTier.LEGEND -> HolographicOverlay(Modifier.fillMaxSize())
                    RarityTier.GOLD -> GoldShimmerOverlay(Modifier.fillMaxSize())
                    RarityTier.COMMON -> Unit
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        sticker.name.uppercase(),
                        color = TextWhite,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        CrestImage(
                            url = crestUrl,
                            contentDescription = sticker.team,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            sticker.team.uppercase(),
                            color = TextGray,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun cardBackground(tier: RarityTier): Brush = when (tier) {
    RarityTier.LEGEND -> Brush.linearGradient(
        listOf(Color(0xFF1A1030), Color(0xFF0D2840), Color(0xFF281028))
    )
    RarityTier.GOLD -> Brush.linearGradient(
        listOf(Color(0xFF4A380A), Color(0xFF3D2E08), Color(0xFF2A2208))
    )
    RarityTier.COMMON -> Brush.linearGradient(
        listOf(DarkBlueMid, Color(0xFF141C35))
    )
}
