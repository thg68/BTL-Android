package com.example.androidbtl.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf

val LocalThemeIsDark = compositionLocalOf<MutableState<Boolean>> {
    error("LocalThemeIsDark not provided")
}
