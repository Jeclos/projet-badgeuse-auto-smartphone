package com.example.badgeuse_auto.ui.screens

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.badgeuse_auto.data.PresenceViewModel
import com.example.badgeuse_auto.data.SettingsViewModel
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

    // ---- UI STATES ----
    var enterDistance by remember { mutableStateOf("150") }
    var exitDistance by remember { mutableStateOf("150") }
    var enterDelay by remember { mutableStateOf("0") }
    var exitDelay by remember { mutableStateOf("0") }

    var lunchEnabled by remember { mutableStateOf(true) }
    var lunchOutside by remember { mutableStateOf(true) }
    var lunchDuration by remember { mutableStateOf("60") }

    var workName by remember { mutableStateOf("") }
    var workLat by remember { mutableStateOf("") }
    var workLon by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val s = settingsViewModel.loadSettings()
        enterDistance = s.enterDistance.toString()
        exitDistance = s.exitDistance.toString()
        enterDelay = s.enterDelaySec.toString()
        exitDelay = s.exitDelaySec.toString()
        lunchEnabled = s.lunchBreakEnabled
        lunchOutside = s.lunchBreakOutside
        lunchDuration = s.lunchBreakDurationMin.toString()

        presenceViewModel.refreshWorkLocation()?.let {
            workName = it.name
            workLat = it.latitude.toString()
            workLon = it.longitude.toString()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Configuration",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            settingsViewModel.saveSettings(
                                enterDistance.toInt(),
                                exitDistance.toInt(),
                                enterDelay.toInt(),
                                exitDelay.toInt(),
                                lunchEnabled,
                                lunchOutside,
                                lunchDuration.toInt()
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Enregistrer")
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            SettingsCard(Icons.Default.LocationOn, "Détection") {
                NumberField("Distance entrée (m)", enterDistance) { enterDistance = it }
                NumberField("Distance sortie (m)", exitDistance) { exitDistance = it }
            }

            SettingsCard(Icons.Default.Schedule, "Temporisation") {
                NumberField("Entrée (secondes)", enterDelay) { enterDelay = it }
                NumberField("Sortie (secondes)", exitDelay) { exitDelay = it }
            }

            SettingsCard(Icons.Default.LunchDining, "Pause déjeuner") {
                SwitchRow("Activer la pause déjeuner", lunchEnabled) {
                    lunchEnabled = it
                }

                if (lunchEnabled) {
                    RadioGroup(
                        options = listOf("À l'extérieur", "Sur place"),
                        selectedIndex = if (lunchOutside) 0 else 1
                    ) {
                        lunchOutside = it == 0
                    }

                    NumberField("Durée (minutes)", lunchDuration) {
                        lunchDuration = it
                    }
                }
            }

            SettingsCard(Icons.Default.Work, "Lieu de travail") {

                OutlinedTextField(
                    value = workName,
                    onValueChange = { workName = it },
                    label = { Text("Nom du lieu") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row {
                    OutlinedTextField(
                        value = workLat,
                        onValueChange = { workLat = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = workLon,
                        onValueChange = { workLon = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedButton(
                    onClick = {
                        LocationServices
                            .getFusedLocationProviderClient(context)
                            .lastLocation
                            .addOnSuccessListener { loc: Location? ->
                                loc?.let {
                                    workLat = it.latitude.toString()
                                    workLon = it.longitude.toString()
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Utiliser ma position actuelle")
                }
            }
        }
    }
}

/* ---------- COMPONENTS ---------- */

@Composable
fun SettingsCard(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.all(Char::isDigit)) onValueChange(it) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun RadioGroup(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    options.forEachIndexed { index, label ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = index == selectedIndex,
                onClick = { onSelect(index) }
            )
            Text(label)
        }
    }
}
