package com.nejracoric.digitalnialbum.ui.pack

import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nejracoric.digitalnialbum.ui.components.FifaPackBackground
import com.nejracoric.digitalnialbum.ui.components.StickerImage
import com.nejracoric.digitalnialbum.ui.theme.FifaGold
import com.nejracoric.digitalnialbum.ui.theme.FifaGreen
import com.nejracoric.digitalnialbum.ui.theme.FifaNavy
import com.nejracoric.digitalnialbum.util.ShakeDetector

@Composable
fun PackScreen(
    viewModel: PackViewModel,
    showBack: Boolean = true,
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val shake by animateFloatAsState(
        if (state.opening) 10f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "shakeRot"
    )
    val scale by animateFloatAsState(
        if (state.opening) 1.1f else 1f,
        label = "packScale"
    )

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(SensorManager::class.java)
        val sensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
        val detector = ShakeDetector { viewModel.onShakeDetected() }
        sensorManager.registerListener(detector, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(detector) }
    }

    FifaPackBackground {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                "OPEN PACK",
                style = MaterialTheme.typography.labelLarge,
                color = FifaGold,
                fontWeight = FontWeight.Bold
            )
            if (!state.isOnline) {
                Text("Potreban internet", color = MaterialTheme.colorScheme.error)
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .rotate(shake),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0)),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = FifaGold
                        )
                        Text(
                            "Protresi telefon",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            "5 nasumičnih sličica",
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        if (state.opening) {
                            CircularProgressIndicator(
                                Modifier.padding(top = 20.dp),
                                color = FifaGold
                            )
                        }
                    }
                }
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = { viewModel.openPack() },
                enabled = state.isOnline && !state.opening,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FifaGreen,
                    contentColor = FifaNavy
                )
            ) {
                Text("OTVORI PAKETIĆ", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
            }
            AnimatedVisibility(
                visible = state.lastPack.isNotEmpty(),
                enter = fadeIn() + scaleIn(initialScale = 0.7f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "DOBIO SI",
                        style = MaterialTheme.typography.labelLarge,
                        color = FifaGold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(state.lastPack) { index, sticker ->
                            Box(Modifier.scale(1f)) {
                                StickerImage(
                                    url = sticker.imageUrl,
                                    name = sticker.name,
                                    size = 88.dp,
                                    isGolden = sticker.isGolden
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
