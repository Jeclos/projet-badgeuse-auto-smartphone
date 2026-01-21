package com.example.badgeuse_auto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.badgeuse_auto.ui.screens.NumberField

@Composable
fun TimeRangePicker(
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    onStartTimeChange: (hour: Int, minute: Int) -> Unit,
    onEndTimeChange: (hour: Int, minute: Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TimePickerRow(
            label = "Début de la plage",
            hour = startHour,
            minute = startMinute,
            onTimeChanged = onStartTimeChange
        )

        TimePickerRow(
            label = "Fin de la plage",
            hour = endHour,
            minute = endMinute,
            onTimeChanged = onEndTimeChange
        )
    }
}

@Composable
private fun TimePickerRow(
    label: String,
    hour: Int,
    minute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit
) {
    var hourText by remember(hour) { mutableStateOf(hour.toString()) }
    var minuteText by remember(minute) { mutableStateOf(minute.toString()) }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            NumberField(
                label = "Heure",
                value = hourText,
                range = 0..23,
                modifier = Modifier.weight(1f)
            ) { newHour ->
                hourText = newHour
                newHour.toIntOrNull()?.let {
                    onTimeChanged(it, minute)
                }
            }

            NumberField(
                label = "Minute",
                value = minuteText,
                range = 0..59,
                modifier = Modifier.weight(1f)
            ) { newMinute ->
                minuteText = newMinute
                newMinute.toIntOrNull()?.let {
                    onTimeChanged(hour, it)
                }
            }
        }
    }
}
