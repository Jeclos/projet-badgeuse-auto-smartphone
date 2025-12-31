package com.example.badgeuse_auto.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.badgeuse_auto.data.PresenceEntity
import java.text.SimpleDateFormat
import java.util.*

/* ================================================================ */
/* EDIT PRESENCE DIALOG                                             */
/* ================================================================ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPresenceDialog(
    entry: PresenceEntity,
    onDismiss: () -> Unit,
    onValidate: (PresenceEntity) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var enterTime by remember { mutableStateOf(entry.enterTime) }
    var exitTime by remember { mutableStateOf(entry.exitTime) }

    var showEnterTimePicker by remember { mutableStateOf(false) }
    var showExitTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    fun showDatePicker(current: Long, onSelected: (Long) -> Unit) {
        calendar.timeInMillis = current
        DatePickerDialog(
            context,
            { _, y, m, d ->
                calendar.set(y, m, d)
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
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                /* ---------------- ENTRÉE ---------------- */

                SectionTitle("Entrée")

                ReadOnlyField(
                    label = "Date",
                    value = dateFormatter.format(Date(enterTime)),
                    onClick = {
                        showDatePicker(enterTime) { enterTime = it }
                    }
                )

                ReadOnlyField(
                    label = "Heure",
                    value = timeFormatter.format(Date(enterTime)),
                    onClick = { showEnterTimePicker = true }
                )

                /* ---------------- SORTIE ---------------- */

                if (exitTime != null) {
                    Spacer(Modifier.height(8.dp))
                    SectionTitle("Sortie")

                    ReadOnlyField(
                        label = "Date",
                        value = dateFormatter.format(Date(exitTime!!)),
                        onClick = {
                            showDatePicker(exitTime!!) { exitTime = it }
                        }
                    )

                    ReadOnlyField(
                        label = "Heure",
                        value = timeFormatter.format(Date(exitTime!!)),
                        onClick = { showExitTimePicker = true }
                    )
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

    /* ---------------- TIME PICKERS ---------------- */

    if (showEnterTimePicker) {
        MaterialTimePickerDialog(
            initialTime = enterTime,
            onDismiss = { showEnterTimePicker = false },
            onConfirm = {
                enterTime = it
                showEnterTimePicker = false
            }
        )
    }

    if (showExitTimePicker && exitTime != null) {
        MaterialTimePickerDialog(
            initialTime = exitTime!!,
            onDismiss = { showExitTimePicker = false },
            onConfirm = {
                exitTime = it
                showExitTimePicker = false
            }
        )
    }
}

/* ================================================================ */
/* MATERIAL TIME PICKER (FIABLE & THEME-AWARE)                       */
/* ================================================================ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialTimePickerDialog(
    initialTime: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val calendar = remember {
        Calendar.getInstance().apply { timeInMillis = initialTime }
    }

    val timeState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choisir l'heure") },
        text = {
            TimePicker(state = timeState)
        },
        confirmButton = {
            TextButton(onClick = {
                calendar.set(Calendar.HOUR_OF_DAY, timeState.hour)
                calendar.set(Calendar.MINUTE, timeState.minute)
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

/* ================================================================ */
/* UI HELPERS                                                        */
/* ================================================================ */

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ReadOnlyField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = true,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}
