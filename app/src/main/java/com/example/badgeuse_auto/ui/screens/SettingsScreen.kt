package com.example.badgeuse_auto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    var enterDistance by remember { mutableStateOf("150") }
    var exitDistance by remember { mutableStateOf("150") }
    var enterDelay by remember { mutableStateOf("0") }
    var exitDelay by remember { mutableStateOf("0") }

    // Charger les paramètres
    LaunchedEffect(Unit) {
        val s = viewModel.loadSettings()
        enterDistance = s.enterDistance.toString()
        exitDistance = s.exitDistance.toString()
        enterDelay = s.enterDelaySec.toString()
        exitDelay = s.exitDelaySec.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    scope.launch {
                        viewModel.saveSettings(
                            enterDistance.toInt(),
                            exitDistance.toInt(),
                            enterDelay.toInt(),
                            exitDelay.toInt()
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
