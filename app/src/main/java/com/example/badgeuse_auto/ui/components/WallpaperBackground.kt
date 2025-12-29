package com.example.badgeuse_auto.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.sin

@Composable
fun WallpaperBackground(
    spacingDp: Int = 150
) {
    val isDark = isSystemInDarkTheme()

    /* ⏱️ Temps */
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            time += 0.025f
            delay(16)
        }
    }

    /* 🎨 Couleur contrastée */
    val base = if (isDark)
        MaterialTheme.colorScheme.inverseSurface
    else
        MaterialTheme.colorScheme.onBackground

    val colors = listOf(
        base.copy(alpha = 0.18f),
        base.copy(alpha = 0.12f),
        base.copy(alpha = 0.08f)
    )

    Canvas(modifier = Modifier.fillMaxSize()) {

        val spacing = spacingDp.dp.toPx()
        val baseSize = 40.dp.toPx()   // 🔺 plus gros
        val cols = ceil(size.width / spacing).toInt()
        val rows = ceil(size.height / spacing).toInt()

        for (x in 0..cols) {
            for (y in 0..rows) {

                val wave = sin(time + x * 0.35f + y * 0.25f)

                /* 🌊 Effet profondeur */
                val depth = (wave + 1f) / 2f
                val scale = 0.6f + depth * 0.9f   // avant / arrière
                val rotation = 45f + wave * 60f  // 🔥 rotation massive

                val cx = x * spacing + wave * 18f
                val cy = y * spacing + wave * 14f
                val sizePx = baseSize * scale

                rotate(rotation, pivot = Offset(cx, cy)) {
                    drawRect(
                        color = colors[(x + y) % colors.size],
                        topLeft = Offset(
                            cx - sizePx / 2,
                            cy - sizePx / 2
                        ),
                        size = Size(sizePx, sizePx)
                    )
                }
            }
        }
    }
}
