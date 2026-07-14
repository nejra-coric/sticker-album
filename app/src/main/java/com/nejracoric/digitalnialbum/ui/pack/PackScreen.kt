package com.nejracoric.digitalnialbum.ui.pack

import android.hardware.SensorManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxkach.swipingcards.SwipingCards
import com.nejracoric.digitalnialbum.R
import com.nejracoric.digitalnialbum.data.model.Sticker
import com.nejracoric.digitalnialbum.ui.components.GlassBackground
import com.nejracoric.digitalnialbum.ui.components.PlayerGlassCard
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan
import com.nejracoric.digitalnialbum.ui.theme.TextGray
import com.nejracoric.digitalnialbum.ui.theme.TextWhite
import com.nejracoric.digitalnialbum.util.ShakeDetector
import com.nejracoric.digitalnialbum.util.resolveCrestUrl

@Composable
fun PackScreen(
    viewModel: PackViewModel,
    showBack: Boolean = true,
    onBack: () -> Unit = {},
    onOpenDetail: (Int) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val hasPack = state.lastPack.isNotEmpty()
    val scale by animateFloatAsState(
        if (state.opening) 1.06f else 1f,
        animationSpec = spring(),
        label = "packScale"
    )
    val shake by animateFloatAsState(
        if (state.opening) 4f else 0f,
        animationSpec = tween(120),
        label = "packShake"
    )

    DisposableEffect(Unit) {
        val sm = context.getSystemService(SensorManager::class.java)
        val sensor = sm.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
        val detector = ShakeDetector { viewModel.onShakeDetected() }
        sm.registerListener(detector, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sm.unregisterListener(detector) }
    }

    GlassBackground {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "OTVORI NOVI",
                color = NeonCyan,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                "PAKETIĆ SLIČICA",
                color = NeonCyan,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            AnimatedContent(
                targetState = hasPack,
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(280))
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "packContent"
            ) { showCards ->
                if (showCards) {
                    PackSwipeDeck(
                        cards = state.lastPack,
                        crestUrls = state.crestUrls,
                        onOpenDetail = onOpenDetail
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.pack_foil),
                            contentDescription = "Paketić sličica",
                            modifier = Modifier
                                .height(300.dp)
                                .scale(scale)
                                .rotate(shake)
                                .clickable(
                                    enabled = state.isOnline && !state.opening
                                ) { viewModel.openPack() },
                            contentScale = ContentScale.Fit
                        )
                        if (state.opening) {
                            CircularProgressIndicator(
                                color = GoldAccent,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }

            if (!hasPack) {
                ShakeHintRow()
                Spacer(Modifier.height(8.dp))
            }

            if (!state.isOnline) {
                Text(
                    "Potreban internet",
                    color = Color(0xFFFF6B6B),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            state.error?.let {
                Text(
                    it,
                    color = Color(0xFFFFB74D),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            if (hasPack) {
                Text(
                    "Prevuci kartice ili tapni za detalj",
                    color = TextGray,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
                Text(
                    text = "Otvori novi paketić",
                    color = GoldAccent,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(
                            enabled = state.isOnline && !state.opening
                        ) { viewModel.openPack() }
                        .padding(vertical = 8.dp)
                )
                if (state.opening) {
                    CircularProgressIndicator(
                        color = GoldAccent,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(bottom = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                Text(
                    "Tapni paketić ili protresi telefon",
                    color = TextGray,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun PackSwipeDeck(
    cards: List<Sticker>,
    crestUrls: Map<String, String>,
    onOpenDetail: (Int) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Tvoje sličice",
            color = TextWhite,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        SwipingCards(
            cards = cards,
            key = { it.id },
            modifier = Modifier
                .fillMaxWidth(0.62f)
                .aspectRatio(2f / 3.1f),
            maxVisibleCards = minOf(3, cards.size.coerceAtLeast(1)),
            maxRotationY = 28f,
            swipeThresholdFraction = 0.18f
        ) { sticker ->
            PlayerGlassCard(
                sticker = sticker,
                crestUrl = resolveCrestUrl(sticker.team, crestUrls),
                onClick = { onOpenDetail(sticker.id) },
                modifier = Modifier.fillMaxSize(),
                fillHeight = true
            )
        }
    }
}

@Composable
private fun ShakeHintRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Default.PhoneAndroid,
            null,
            tint = NeonCyan.copy(0.7f),
            modifier = Modifier
                .size(22.dp)
                .rotate(-12f)
        )
        Text(
            "Protresi telefon za otvaranje paketića",
            color = NeonCyan,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Icon(
            Icons.Default.PhoneAndroid,
            null,
            tint = NeonCyan.copy(0.7f),
            modifier = Modifier
                .size(22.dp)
                .rotate(12f)
        )
    }
}
