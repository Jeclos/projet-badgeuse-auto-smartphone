package com.example.badgeuse_auto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    // Chargement depuis la DB
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
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            // ----------------------------
            // Paramètres existants
            // ----------------------------
            OutlinedTextField(
                value = enterDistance,
                onValueChange = { enterDistance = it },
                label = { Text("Distance entrée (m)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = exitDistance,
                onValueChange = { exitDistance = it },
                label = { Text("Distance sortie (m)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = enterDelay,
                onValueChange = { enterDelay = it },
                label = { Text("Temporisation entrée (sec)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = exitDelay,
                onValueChange = { exitDelay = it },
                label = { Text("Temporisation sortie (sec)") },
                modifier = Modifier.fillMaxWidth()
            )

            // ----------------------------
            // Pause déjeuner
            // ----------------------------
            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Pause déjeuner",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = lunchOutside,
                        onClick = { lunchOutside = true }
                    )
                    Text(
                        "À l'extérieur",
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    RadioButton(
                        selected = !lunchOutside,
                        onClick = { lunchOutside = false }
                    )
                    Text("Sur place")
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = lunchDuration,
                    onValueChange = { lunchDuration = it },
                    label = { Text("Durée de la pause (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ----------------------------
            // Bouton sauvegarde
            // ----------------------------
            Spacer(Modifier.height(24.dp))

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
                        ) {
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enregistrer")
            }
        }
    }
}
