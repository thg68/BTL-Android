package com.example.androidbtl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.androidbtl.ui.navigation.AppNavigation
import com.example.androidbtl.ui.theme.AndroidBTLTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidBTLTheme {
                AppNavigation()
            }
        }
    }
}
