package com.nejracoric.digitalnialbum.ui.pack

import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.nejracoric.digitalnialbum.R
import com.nejracoric.digitalnialbum.ui.components.GlassBackground
import com.nejracoric.digitalnialbum.ui.components.GlassCard
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
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
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

            ShakeHintRow()
            Spacer(Modifier.height(8.dp))

            if (!state.isOnline) {
                Text("Potreban internet", color = Color(0xFFFF6B6B), style = MaterialTheme.typography.bodySmall)
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

            Text(
                "Tapni paketić ili protresi telefon",
                color = TextGray,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            AnimatedVisibility(
                visible = state.lastPack.isNotEmpty(),
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    corner = 20.dp
                ) {
                    Column(Modifier.padding(vertical = 14.dp)) {
                        Text(
                            "Tvoje sličice",
                            color = TextWhite,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )
                        LazyRow(
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(state.lastPack) { index, sticker ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(400, delayMillis = index * 120)) +
                                        slideInVertically(tween(400, delayMillis = index * 120)) { it / 3 }
                                ) {
                                    Box(Modifier.width(150.dp)) {
                                        PlayerGlassCard(
                                            sticker = sticker,
                                            crestUrl = resolveCrestUrl(sticker.team, state.crestUrls),
                                            onClick = { onOpenDetail(sticker.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
