package com.example.badgeuse_auto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun BadgeuseTheme(
    style: AppStyle = AppStyle.PRO,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when {
        darkTheme -> DarkColors
        style == AppStyle.FUN -> FunLight
        style == AppStyle.MINIMAL -> MinimalLight
        else -> ProLight
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
