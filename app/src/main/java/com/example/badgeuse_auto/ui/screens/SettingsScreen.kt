package com.example.badgeuse_auto.ui.screens

import android.annotation.SuppressLint
import android.location.Location
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.badgeuse_auto.data.SettingsViewModel
import com.example.badgeuse_auto.data.PresenceViewModel
import com.example.badgeuse_auto.data.WorkLocationEntity
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    presenceViewModel: PresenceViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // ----------------------------
    // États UI - Settings
    // ----------------------------
    var enterDistance by remember { mutableStateOf("150") }
    var exitDistance by remember { mutableStateOf("150") }
    var enterDelay by remember { mutableStateOf("0") }
    var exitDelay by remember { mutableStateOf("0") }

    var lunchEnabled by remember { mutableStateOf(true) }
    var lunchOutside by remember { mutableStateOf(true) }
    var lunchDuration by remember { mutableStateOf("60") }

    // ----------------------------
    // États UI - Lieu de travail
    // ----------------------------
    var workName by remember { mutableStateOf("") }
    var workLat by remember { mutableStateOf("") }
    var workLon by remember { mutableStateOf("") }

    // ----------------------------
    // Chargement DB
    // ----------------------------
    LaunchedEffect(Unit) {
        val s = settingsViewModel.loadSettings()
        enterDistance = s.enterDistance.toString()
        exitDistance = s.exitDistance.toString()
        enterDelay = s.enterDelaySec.toString()
        exitDelay = s.exitDelaySec.toString()
        lunchEnabled = s.lunchBreakEnabled
        lunchOutside = s.lunchBreakOutside
        lunchDuration = s.lunchBreakDurationMin.toString()

        val work = presenceViewModel.refreshWorkLocation()
        if (work != null) {
            workName = work.name
            workLat = work.latitude.toString()
            workLon = work.longitude.toString()
        }
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
            SettingsCard(Icons.Default.LocationOn, "Détection") {
                NumberField("Distance entrée (m)", enterDistance) { enterDistance = it }
                Spacer(Modifier.height(8.dp))
                NumberField("Distance sortie (m)", exitDistance) { exitDistance = it }
            }

            // ----------------------------
            // Temporisation
            // ----------------------------
            SettingsCard(Icons.Default.Schedule, "Temporisation") {
                NumberField("Entrée (secondes)", enterDelay) { enterDelay = it }
                Spacer(Modifier.height(8.dp))
                NumberField("Sortie (secondes)", exitDelay) { exitDelay = it }
            }

            // ----------------------------
            // Pause déjeuner
            // ----------------------------
            SettingsCard(Icons.Default.LunchDining, "Pause déjeuner") {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = lunchEnabled, onCheckedChange = { lunchEnabled = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Activer la pause déjeuner")
                }

                if (lunchEnabled) {
                    Spacer(Modifier.height(12.dp))
                    Text("Type de pause")

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = lunchOutside, onClick = { lunchOutside = true })
                        Text("À l'extérieur", Modifier.padding(end = 16.dp))

                        RadioButton(selected = !lunchOutside, onClick = { lunchOutside = false })
                        Text("Sur place")
                    }

                    Spacer(Modifier.height(8.dp))
                    NumberField("Durée (minutes)", lunchDuration) { lunchDuration = it }
                }
            }

            // ----------------------------
            // Lieu de travail
            // ----------------------------
            SettingsCard(Icons.Default.LocationOn, "Lieu de travail") {

                OutlinedTextField(
                    value = workName,
                    onValueChange = { workName = it },
                    label = { Text("Nom du lieu") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = workLat,
                    onValueChange = { workLat = it },
                    label = { Text("Latitude") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = workLon,
                    onValueChange = { workLon = it },
                    label = { Text("Longitude") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        val client = LocationServices.getFusedLocationProviderClient(context)
                        client.lastLocation.addOnSuccessListener { loc: Location? ->
                            if (loc != null) {
                                workLat = loc.latitude.toString()
                                workLon = loc.longitude.toString()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Utiliser ma localisation actuelle")
                }
            }

            // ----------------------------
            // Sauvegarde globale
            // ----------------------------
            Button(
                onClick = {
                    scope.launch {

                        settingsViewModel.saveSettings(
                            enterDistance = enterDistance.toInt(),
                            exitDistance = exitDistance.toInt(),
                            enterDelaySec = enterDelay.toInt(),
                            exitDelaySec = exitDelay.toInt(),
                            lunchEnabled = lunchEnabled,
                            lunchOutside = lunchOutside,
                            lunchDurationMin = lunchDuration.toInt()
                        )

                        presenceViewModel.saveWorkLocation(
                            WorkLocationEntity(
                                name = workName,
                                latitude = workLat.toDouble(),
                                longitude = workLon.toDouble()
                            )
                        )

                        onBack()
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
