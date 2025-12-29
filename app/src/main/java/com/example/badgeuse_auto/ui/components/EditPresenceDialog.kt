package com.example.badgeuse_auto.ui.components

import android.app.DatePickerDialog
import android.widget.NumberPicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.badgeuse_auto.data.PresenceEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditPresenceDialog(
    entry: PresenceEntity,
    onDismiss: () -> Unit,
    onValidate: (PresenceEntity) -> Unit
) {
    val context = LocalContext.current

    // ✅ ALIGNÉ AVEC PresenceEntity
    var enterTime by remember { mutableStateOf(entry.enterTime) }
    var exitTime by remember { mutableStateOf(entry.exitTime) }

    var showTimePickerForEnter by remember { mutableStateOf(false) }
    var showTimePickerForExit by remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }

    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }
    val timeFormatter = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    fun showDatePicker(current: Long, onSelected: (Long) -> Unit) {
        calendar.timeInMillis = current
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                onSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier la présence") },

        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                /* -------- ENTRÉE -------- */
                Text(
                    text = "Entrée : ${dateFormatter.format(Date(enterTime))} à ${timeFormatter.format(Date(enterTime))}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker(enterTime) { enterTime = it }
                        }
                )

                Button(onClick = { showTimePickerForEnter = true }) {
                    Text("Modifier heure d'entrée")
                }

                /* -------- SORTIE -------- */
                if (exitTime != null) {

                    Text(
                        text = "Sortie : ${dateFormatter.format(Date(exitTime!!))} à ${timeFormatter.format(Date(exitTime!!))}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDatePicker(exitTime!!) { exitTime = it }
                            }
                    )

                    Button(onClick = { showTimePickerForExit = true }) {
                        Text("Modifier heure de sortie")
                    }
                }
            }
        },

        confirmButton = {
            TextButton(onClick = {
                onValidate(
                    entry.copy(
                        enterTime = enterTime,
                        exitTime = exitTime
                    )
                )
            }) {
                Text("Valider")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )

    /* -------- TIME PICKERS -------- */

    if (showTimePickerForEnter) {
        ClassicTimePickerDialog(
            initialTime = enterTime,
            onDismiss = { showTimePickerForEnter = false },
            onConfirm = {
                enterTime = it
                showTimePickerForEnter = false
            }
        )
    }

    if (showTimePickerForExit && exitTime != null) {
        ClassicTimePickerDialog(
            initialTime = exitTime!!,
            onDismiss = { showTimePickerForExit = false },
            onConfirm = {
                exitTime = it
                showTimePickerForExit = false
            }
        )
    }
}

/* ------------------------------------------------------------------ */
/* TIME PICKER                                                         */
/* ------------------------------------------------------------------ */

@Composable
fun ClassicTimePickerDialog(
    initialTime: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    calendar.timeInMillis = initialTime

    var hour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choisir l'heure") },

        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                AndroidView(
                    factory = { context ->
                        NumberPicker(context).apply {
                            minValue = 0
                            maxValue = 23
                            value = hour
                            setFormatter { String.format("%02d", it) }
                            setOnValueChangedListener { _, _, new ->
                                hour = new
                            }
                        }
                    }
                )

                AndroidView(
                    factory = { context ->
                        NumberPicker(context).apply {
                            minValue = 0
                            maxValue = 59
                            value = minute
                            setFormatter { String.format("%02d", it) }
                            setOnValueChangedListener { _, _, new ->
                                minute = new
                            }
                        }
                    }
                )
            }
        },

        confirmButton = {
            TextButton(onClick = {
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                onConfirm(calendar.timeInMillis)
            }) {
                Text("OK")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
