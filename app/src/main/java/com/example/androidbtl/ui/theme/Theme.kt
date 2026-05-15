package com.example.androidbtl.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BrandYellow,
    onPrimary = TextPrimary,
    secondary = BrandYellowLight,
    onSecondary = TextPrimary,
    tertiary = ActionRed,
    onTertiary = NeutralWhite,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = NeutralDarkGray,
)

private val LightColorScheme = lightColorScheme(
    primary = BrandYellow,
    onPrimary = TextPrimary,
    secondary = BrandYellowDark,
    onSecondary = NeutralWhite,
    tertiary = ActionRed,
    onTertiary = NeutralWhite,
    background = NeutralBackground,
    onBackground = TextPrimary,
    surface = NeutralWhite,
    onSurface = TextPrimary,
    surfaceVariant = NeutralBackground,
    onSurfaceVariant = TextSecondary,
    outline = NeutralGray,
)

@Composable
fun AndroidBTLTheme(
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDarkState = remember { mutableStateOf(systemDark) }
    val isDark by isDarkState

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    CompositionLocalProvider(LocalThemeIsDark provides isDarkState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
