package com.nejracoric.digitalnialbum.ui.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nejracoric.digitalnialbum.ui.components.FifaBackground
import com.nejracoric.digitalnialbum.ui.theme.FifaGold
import com.nejracoric.digitalnialbum.ui.theme.FifaGreen
import com.nejracoric.digitalnialbum.ui.theme.FifaGray
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: (goOnboarding: Boolean) -> Unit,
    onboardingDone: Boolean
) {
    LaunchedEffect(Unit) {
        delay(1500)
        onFinished(!onboardingDone)
    }
    FifaBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "WC 2026",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = FifaGold
            )
            Text(
                "STICKER CONNECT",
                style = MaterialTheme.typography.labelLarge,
                color = FifaGray,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )
            CircularProgressIndicator(color = FifaGreen)
        }
    }
}
