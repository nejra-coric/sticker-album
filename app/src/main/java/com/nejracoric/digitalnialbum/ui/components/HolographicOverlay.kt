package com.nejracoric.digitalnialbum.ui.components

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.MagentaAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan

@Composable
fun HolographicOverlay(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val sm = context.getSystemService(SensorManager::class.java)
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                tiltX = (event.values[0] / 10f).coerceIn(-1f, 1f)
                tiltY = (event.values[1] / 10f).coerceIn(-1f, 1f)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sm.unregisterListener(listener) }
    }

    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width * (0.5f + tiltX * 0.25f)
            val cy = size.height * (0.5f + tiltY * 0.25f)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonCyan.copy(0.35f),
                        MagentaAccent.copy(0.2f),
                        GoldAccent.copy(0.15f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = size.width * 0.8f
                )
            )
        }
    }
}
