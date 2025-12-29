package com.example.badgeuse_auto.ui.components
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme

@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {

        WallpaperBackground()

        Box(
            Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Transparent)
        ) {
            content()
        }
    }
}

