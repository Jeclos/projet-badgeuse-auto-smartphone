package com.example.badgeuse_auto.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FloatingIllustratedClock() {

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // 🔢 Écran en PX
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // ⏰ Taille horloge
    val sizeDp = 140.dp
    val sizePx = with(density) { sizeDp.toPx() }

    // 📍 Position & vitesse en PX
    var position by remember { mutableStateOf(Offset(200f, 200f)) }
    var velocity by remember { mutableStateOf(Offset(3.5f, 2.8f)) }
    var rotation by remember { mutableStateOf(0f) }

    /* 🔁 BOUCLE DE MOUVEMENT INFINIE */
    LaunchedEffect(Unit) {
        while (true) {
            position += velocity
            rotation += 0.6f

            // 🔄 REBONDS PARFAITS
            if (position.x <= 0f || position.x >= screenWidthPx - sizePx) {
                velocity = Offset(-velocity.x, velocity.y)
            }

            if (position.y <= 0f || position.y >= screenHeightPx - sizePx) {
                velocity = Offset(velocity.x, -velocity.y)
            }

            delay(16) // ~60 FPS
        }
    }

    // ⏱️ Heure actuelle
    val calendar = remember { Calendar.getInstance() }
    calendar.timeInMillis = System.currentTimeMillis()

    val hour = calendar.get(Calendar.HOUR)
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)

    Box(
        modifier = Modifier
            .offset(
                x = with(density) { position.x.toDp() },
                y = with(density) { position.y.toDp() }
            )
            .size(sizeDp)
            .rotate(rotation)
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {

            val center = size.minDimension / 2f
            val radius = center * 0.85f

            fun drawHand(
                angle: Float,
                lengthRatio: Float,
                stroke: Float,
                color: Color
            ) {
                val rad = Math.toRadians(angle.toDouble() - 90)
                drawLine(
                    color = color,
                    start = Offset(center, center),
                    end = Offset(
                        center + cos(rad).toFloat() * radius * lengthRatio,
                        center + sin(rad).toFloat() * radius * lengthRatio
                    ),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }

            // 🕰️ AIGUILLES
            drawHand(hour * 30f + minute / 2f, 0.5f, 10f, Color.Black)
            drawHand(minute * 6f, 0.7f, 7f, Color.DarkGray)
            drawHand(second * 6f, 0.9f, 3f, Color.Red)
        }
    }
}
