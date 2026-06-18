package com.nejracoric.digitalnialbum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.nejracoric.digitalnialbum.ui.navigation.AppNavHost
import com.nejracoric.digitalnialbum.ui.theme.DigitalniAlbumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DigitalniAlbumTheme {
                AppNavHost()
            }
        }
    }
}
