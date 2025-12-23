package com.example.badgeuse_auto.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color



private val FunDarkColorScheme = darkColorScheme(
    primary = FunPurple,
    secondary = FunPink,
    tertiary = FunGreen,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White
)


@Composable
fun Badgeuse_AutoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FunDarkColorScheme,
        typography = Typography(),
        content = content
    )
}
