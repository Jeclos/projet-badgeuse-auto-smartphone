package com.example.badgeuse_auto.ui.components
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
@Composable
fun WorkStatusBar(isWorking: Boolean, modifier: Modifier = Modifier) {
    val color by animateColorAsState(
        if (isWorking) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.error,
        label = ""
    )

    Box(
        modifier
            .height(28.dp)
            .width(if (isWorking) 220.dp else 160.dp)
            .clip(RoundedCornerShape(50))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (isWorking) "EN TRAVAIL" else "HORS TRAVAIL",
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
