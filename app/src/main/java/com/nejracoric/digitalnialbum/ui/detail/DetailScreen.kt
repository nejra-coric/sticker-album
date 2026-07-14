package com.nejracoric.digitalnialbum.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.ui.components.CrestImage
import com.nejracoric.digitalnialbum.ui.components.GlassBackground
import com.nejracoric.digitalnialbum.ui.components.GlassCard
import com.nejracoric.digitalnialbum.ui.components.GoldShimmerOverlay
import com.nejracoric.digitalnialbum.ui.components.HolographicOverlay
import com.nejracoric.digitalnialbum.ui.components.StickerImage
import com.nejracoric.digitalnialbum.ui.theme.DarkBlueMid
import com.nejracoric.digitalnialbum.ui.theme.GlassBorder
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.MagentaAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan
import com.nejracoric.digitalnialbum.ui.theme.TextGray
import com.nejracoric.digitalnialbum.ui.theme.TextWhite
import com.nejracoric.digitalnialbum.util.RarityTier
import com.nejracoric.digitalnialbum.util.ShareUtil
import com.nejracoric.digitalnialbum.util.displayRating
import com.nejracoric.digitalnialbum.util.effectiveImageUrl
import com.nejracoric.digitalnialbum.util.rarityLabel
import com.nejracoric.digitalnialbum.util.rarityTier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HeroShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 20.dp, bottomEnd = 20.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val sticker = state.sticker

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Nazad",
                                tint = TextWhite
                            )
                        }
                    },
                    actions = {
                        if (sticker != null) {
                            IconButton(onClick = { viewModel.toggleWishlist() }) {
                                Icon(
                                    if (sticker.isWished) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Sačuvano",
                                    tint = if (sticker.isWished) NeonCyan else TextWhite
                                )
                            }
                            IconButton(onClick = { ShareUtil.shareSticker(context, sticker) }) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Podijeli",
                                    tint = GoldAccent
                                )
                            }
                            IconButton(onClick = { viewModel.toggleFavorite() }) {
                                Icon(
                                    if (sticker.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorit",
                                    tint = if (sticker.isFavorite) GoldAccent else TextWhite
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = TextWhite,
                        actionIconContentColor = TextWhite
                    )
                )
            }
        ) { padding ->
            when {
                state.loading -> Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonCyan)
                }
                sticker == null -> Text(
                    state.message ?: "Greška",
                    modifier = Modifier.padding(padding).padding(16.dp),
                    color = TextWhite
                )
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    DetailHeroCard(
                        sticker = sticker,
                        crestUrl = state.crestUrl
                    )
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        corner = 18.dp
                    ) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            DetailInfoRow(
                                label = "Reprezentacija",
                                value = sticker.team.uppercase(),
                                trailing = {
                                    CrestImage(
                                        url = state.crestUrl,
                                        contentDescription = sticker.team,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            )
                            DetailDivider()
                            DetailInfoRow("Pozicija", sticker.position)
                            DetailDivider()
                            DetailInfoRow(
                                label = "Datum dobijanja",
                                value = sticker.obtainedAt?.let { formatDate(it) } ?: "—"
                            )
                            DetailDivider()
                            DetailInfoRow("Rijetkost", sticker.rarityLabel())
                            DetailDivider()
                            DetailInfoRow("Broj sličice", sticker.number.padStart(3, '0'))
                            DetailDivider()
                            DetailInfoRow(
                                label = "Status",
                                value = when {
                                    sticker.ownedCount > 1 -> "Skupljeno (x${sticker.ownedCount})"
                                    sticker.owned -> "Skupljeno"
                                    else -> "Nedostaje"
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailHeroCard(
    sticker: Sticker,
    crestUrl: String?
) {
    val tier = sticker.rarityTier()
    val golden = tier == RarityTier.LEGEND || tier == RarityTier.GOLD
    val accentColor = when (tier) {
        RarityTier.LEGEND, RarityTier.GOLD -> GoldAccent
        RarityTier.RARE -> MagentaAccent
        RarityTier.COMMON -> NeonCyan
    }
    val borderBrush = if (golden) {
        Brush.linearGradient(listOf(GoldAccent, Color(0xFFFFA500), GoldAccent))
    } else if (tier == RarityTier.RARE) {
        Brush.linearGradient(listOf(MagentaAccent.copy(0.8f), GlassBorder, MagentaAccent.copy(0.4f)))
    } else {
        Brush.linearGradient(listOf(NeonCyan.copy(0.7f), GlassBorder, NeonCyan.copy(0.4f)))
    }

    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(HeroShape)
                .border(2.dp, borderBrush, HeroShape)
                .background(detailCardBackground(tier))
        ) {
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .background(detailCardBackground(tier))
                ) {
                    when (tier) {
                        RarityTier.LEGEND -> HolographicOverlay(Modifier.fillMaxSize())
                        RarityTier.GOLD -> GoldShimmerOverlay(Modifier.fillMaxSize())
                        RarityTier.RARE, RarityTier.COMMON -> Unit
                    }
                    StickerImage(
                        url = sticker.effectiveImageUrl(),
                        name = sticker.name,
                        stickerId = sticker.id,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 28.dp),
                        owned = sticker.owned,
                        isGolden = false,
                        contentScale = ContentScale.Fit
                    )
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "${sticker.displayRating()}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                        Text(
                            sticker.rarityLabel(),
                            style = MaterialTheme.typography.titleSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(0.85f))
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Column {
                            Text(
                                sticker.name.uppercase(),
                                color = TextWhite,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                CrestImage(
                                    url = crestUrl,
                                    contentDescription = sticker.team,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    sticker.team.uppercase(),
                                    color = TextWhite,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextGray, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                value,
                color = TextWhite,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = if (trailing != null) 10.dp else 0.dp)
            )
            trailing?.invoke()
        }
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(color = Color(0x2200E5FF), thickness = 1.dp)
}

@Composable
private fun detailCardBackground(tier: RarityTier): Brush = when (tier) {
    RarityTier.LEGEND -> Brush.linearGradient(
        listOf(Color(0xFF1A1030), Color(0xFF0D2840), Color(0xFF281028))
    )
    RarityTier.GOLD -> Brush.linearGradient(
        listOf(Color(0xFF4A380A), Color(0xFF3D2E08), Color(0xFF2A2208))
    )
    RarityTier.RARE -> Brush.linearGradient(
        listOf(Color(0xFF2A1030), Color(0xFF1A1840), Color(0xFF281828))
    )
    RarityTier.COMMON -> Brush.linearGradient(
        listOf(DarkBlueMid, Color(0xFF141C35))
    )
}

private fun formatDate(ts: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(ts))
