package com.example.badgeuse_auto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.badgeuse_auto.data.SettingsViewModel
import kotlinx.coroutines.launch




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // ----------------------------
    // États UI
    // ----------------------------
    var enterDistance by remember { mutableStateOf("150") }
    var exitDistance by remember { mutableStateOf("150") }
    var enterDelay by remember { mutableStateOf("0") }
    var exitDelay by remember { mutableStateOf("0") }

    var lunchEnabled by remember { mutableStateOf(true) }
    var lunchOutside by remember { mutableStateOf(true) }
    var lunchDuration by remember { mutableStateOf("60") }

    // ----------------------------
    // Chargement DB
    // ----------------------------
    LaunchedEffect(Unit) {
        val s = viewModel.loadSettings()
        enterDistance = s.enterDistance.toString()
        exitDistance = s.exitDistance.toString()
        enterDelay = s.enterDelaySec.toString()
        exitDelay = s.exitDelaySec.toString()
        lunchEnabled = s.lunchBreakEnabled
        lunchOutside = s.lunchBreakOutside
        lunchDuration = s.lunchBreakDurationMin.toString()
    }

    // ----------------------------
    // UI
    // ----------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ----------------------------
            // Détection
            // ----------------------------
            SettingsCard(
                icon = Icons.Default.LocationOn,
                title = "Détection"
            ) {
                NumberField("Distance entrée (m)", enterDistance) { enterDistance = it }
                Spacer(Modifier.height(8.dp))
                NumberField("Distance sortie (m)", exitDistance) { exitDistance = it }
            }

            // ----------------------------
            // Temporisations
            // ----------------------------
            SettingsCard(
                icon = Icons.Default.Schedule,
                title = "Temporisation"
            ) {
                NumberField("Entrée (secondes)", enterDelay) { enterDelay = it }
                Spacer(Modifier.height(8.dp))
                NumberField("Sortie (secondes)", exitDelay) { exitDelay = it }
            }

            // ----------------------------
            // Pause déjeuner
            // ----------------------------
            SettingsCard(
                icon = Icons.Default.LunchDining,
                title = "Pause déjeuner"
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = lunchEnabled,
                        onCheckedChange = { lunchEnabled = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Activer la pause déjeuner")
                }

                if (lunchEnabled) {

                    Spacer(Modifier.height(12.dp))
                    Text("Type de pause")

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = lunchOutside,
                            onClick = { lunchOutside = true }
                        )
                        Text("À l'extérieur", Modifier.padding(end = 16.dp))

                        RadioButton(
                            selected = !lunchOutside,
                            onClick = { lunchOutside = false }
                        )
                        Text("Sur place")
                    }

                    Spacer(Modifier.height(8.dp))
                    NumberField(
                        label = "Durée (minutes)",
                        value = lunchDuration
                    ) { lunchDuration = it }
                }
            }

            // ----------------------------
            // Sauvegarde
            // ----------------------------
            Button(
                onClick = {
                    scope.launch {
                        viewModel.saveSettings(
                            enterDistance = enterDistance.toInt(),
                            exitDistance = exitDistance.toInt(),
                            enterDelaySec = enterDelay.toInt(),
                            exitDelaySec = exitDelay.toInt(),
                            lunchEnabled = lunchEnabled,
                            lunchOutside = lunchOutside,
                            lunchDurationMin = lunchDuration.toInt()
                        ) { onBack() }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enregistrer")
            }
        }
    }
}

/* ----------------------------
   Composants réutilisables
   ---------------------------- */

@Composable
private fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { c -> c.isDigit() }) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}
