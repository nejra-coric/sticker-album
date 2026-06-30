package com.nejracoric.digitalnialbum.ui.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nejracoric.digitalnialbum.ui.components.GlassBackground
import com.nejracoric.digitalnialbum.ui.components.GlassCard
import com.nejracoric.digitalnialbum.ui.theme.GoldAccent
import com.nejracoric.digitalnialbum.ui.theme.NeonCyan
import com.nejracoric.digitalnialbum.ui.theme.TextGray
import com.nejracoric.digitalnialbum.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: (goOnboarding: Boolean) -> Unit,
    onboardingDone: Boolean
) {
    LaunchedEffect(Unit) {
        delay(2200)
        onFinished(!onboardingDone)
    }

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.School, null, tint = NeonCyan, modifier = Modifier.size(28.dp))
                Text(
                    "Odsjek za matematičke i kompjuterske nauke",
                    color = TextGray,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            GlassCard(corner = 24.dp, golden = true) {
                Column(
                    Modifier.padding(horizontal = 36.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "20",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan
                    )
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        "26",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "DIGITALNI ALBUM",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "ZA SKUPLJANJE SLIČICA 2026",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldAccent,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(32.dp))
                Text(
                    "Razvoj mobilnih aplikacija",
                    color = TextGray,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
