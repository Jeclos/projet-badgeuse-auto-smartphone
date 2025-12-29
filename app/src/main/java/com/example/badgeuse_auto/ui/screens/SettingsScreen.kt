package com.example.badgeuse_auto.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import com.example.badgeuse_auto.data.*
import com.example.badgeuse_auto.ui.components.WallpaperBackground
import com.example.badgeuse_auto.ui.theme.AppStyle
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    presenceViewModel: PresenceViewModel,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val settings by settingsViewModel.settingsFlow
        .collectAsState(initial = SettingsEntity())

    val workLocations by presenceViewModel.workLocations.collectAsState()

    /* ---------- UI STATES ---------- */
    var enterDistance by remember { mutableStateOf("") }
    var exitDistance by remember { mutableStateOf("") }
    var enterDelay by remember { mutableStateOf("") }
    var exitDelay by remember { mutableStateOf("") }

    var lunchEnabled by remember { mutableStateOf(false) }
    var lunchOutside by remember { mutableStateOf(true) }
    var lunchDuration by remember { mutableStateOf("") }

    var newWorkName by remember { mutableStateOf("") }
    var newWorkLat by remember { mutableStateOf("") }
    var newWorkLon by remember { mutableStateOf("") }

    var employeeName by remember { mutableStateOf("") }
    var employeeAddress by remember { mutableStateOf("") }
    var employerName by remember { mutableStateOf("") }
    var employerAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    /* ---------- LOAD SETTINGS ---------- */
    LaunchedEffect(settings) {
        enterDistance = settings.enterDistance.toString()
        exitDistance = settings.exitDistance.toString()
        enterDelay = settings.enterDelaySec.toString()
        exitDelay = settings.exitDelaySec.toString()

        lunchEnabled = settings.lunchBreakEnabled
        lunchOutside = settings.lunchBreakOutside
        lunchDuration = settings.lunchBreakDurationMin.toString()

        employeeName = settings.employeeName
        employeeAddress = settings.employeeAddress
        employerName = settings.employerName
        employerAddress = settings.employerAddress
        city = settings.city
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    onClick = {
                        settingsViewModel.saveSettings(
                            enterDistance.toIntOrNull() ?: 0,
                            exitDistance.toIntOrNull() ?: 0,
                            enterDelay.toIntOrNull() ?: 0,
                            exitDelay.toIntOrNull() ?: 0,
                            lunchEnabled,
                            lunchOutside,
                            lunchDuration.toIntOrNull() ?: 0,
                            employeeName,
                            employeeAddress,
                            employerName,
                            employerAddress,
                            city
                        ) {
                            onBack()
                        }
                    }
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Enregistrer")
                }
            }
        }
    ) { padding ->

        Box(Modifier.fillMaxSize()) {
            WallpaperBackground()

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                /* ---------------- IDENTITÉ ---------------- */
                SettingsCard(Icons.Default.Badge, "Identité") {
                    OutlinedTextField(
                        value = employeeName,
                        onValueChange = { employeeName = it },
                        label = { Text("Nom de l’employé") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = employeeAddress,
                        onValueChange = { employeeAddress = it },
                        label = { Text("Adresse de l’employé") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider()

                    OutlinedTextField(
                        value = employerName,
                        onValueChange = { employerName = it },
                        label = { Text("Nom de l’employeur") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = employerAddress,
                        onValueChange = { employerAddress = it },
                        label = { Text("Adresse de l’employeur") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Ville (PDF)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                /* ---------------- DÉTECTION ---------------- */
                SettingsCard(Icons.Default.LocationOn, "Détection") {
                    NumberField("Distance entrée (m)", enterDistance) { enterDistance = it }
                    NumberField("Distance sortie (m)", exitDistance) { exitDistance = it }
                }

                /* ---------------- TEMPORISATION ---------------- */
                SettingsCard(Icons.Default.Schedule, "Temporisation") {
                    NumberField("Entrée (sec)", enterDelay) { enterDelay = it }
                    NumberField("Sortie (sec)", exitDelay) { exitDelay = it }
                }

                /* ---------------- PAUSE ---------------- */
                SettingsCard(Icons.Default.LunchDining, "Pause déjeuner") {
                    SwitchRow("Activer la pause", lunchEnabled) { lunchEnabled = it }

                    if (lunchEnabled) {
                        RadioGroup(
                            listOf("À l'extérieur", "Sur place"),
                            if (lunchOutside) 0 else 1
                        ) { lunchOutside = it == 0 }

                        NumberField("Durée (min)", lunchDuration) { lunchDuration = it }
                    }
                }

                /* ---------------- LIEUX DE TRAVAIL ---------------- */
                SettingsCard(Icons.Default.Work, "Lieux de travail") {

                    workLocations.forEach { loc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(loc.name, fontWeight = FontWeight.Medium)
                                Text(
                                    "${loc.latitude}, ${loc.longitude}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Switch(
                                checked = loc.isActive,
                                onCheckedChange = {
                                    presenceViewModel.setWorkLocationActive(loc, it)
                                }
                            )

                            IconButton(onClick = {
                                presenceViewModel.deleteWorkLocation(loc)
                            }) {
                                Icon(Icons.Default.Delete, null)
                            }
                        }
                        Divider()
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newWorkName,
                        onValueChange = { newWorkName = it },
                        label = { Text("Nom du lieu") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newWorkLat,
                            onValueChange = { newWorkLat = it },
                            label = { Text("Latitude") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(Modifier.width(8.dp))

                        OutlinedTextField(
                            value = newWorkLon,
                            onValueChange = { newWorkLon = it },
                            label = { Text("Longitude") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) return@IconButton

                                LocationServices.getFusedLocationProviderClient(context)
                                    .getCurrentLocation(
                                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                                        null
                                    )
                                    .addOnSuccessListener { location ->
                                        location?.let {
                                            newWorkLat = it.latitude.toString()
                                            newWorkLon = it.longitude.toString()
                                        }
                                    }
                            }
                        ) {
                            Icon(Icons.Default.MyLocation, null)
                        }
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = newWorkName.isNotBlank()
                                && newWorkLat.toDoubleOrNull() != null
                                && newWorkLon.toDoubleOrNull() != null,
                        onClick = {
                            presenceViewModel.addWorkLocation(
                                name = newWorkName,
                                latitude = newWorkLat.toDouble(),
                                longitude = newWorkLon.toDouble()
                            )
                            newWorkName = ""
                            newWorkLat = ""
                            newWorkLon = ""
                        }
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ajouter le lieu")
                    }
                }

                /* ---------------- APPARENCE ---------------- */
                SettingsCard(Icons.Default.Palette, "Apparence") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Style", Modifier.weight(1f))
                        AppStyle.values().forEach {
                            FilterChip(
                                selected = settings.appStyle == it.name,
                                onClick = { settingsViewModel.setAppStyle(it) },
                                label = { Text(it.name) }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Thème", Modifier.weight(1f))
                        ThemeMode.values().forEach { mode ->
                            IconButton(onClick = { settingsViewModel.setThemeMode(mode) }) {
                                Icon(
                                    when (mode) {
                                        ThemeMode.LIGHT -> Icons.Default.LightMode
                                        ThemeMode.DARK -> Icons.Default.DarkMode
                                        ThemeMode.SYSTEM -> Icons.Default.AutoMode
                                    },
                                    null,
                                    tint = if (settings.themeMode == mode)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- UI HELPERS ---------------- */

@Composable
fun SettingsCard(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.SemiBold)
            }
            content()
        }
    }
}

@Composable
fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.isEmpty() || it.all(Char::isDigit)) onValueChange(it) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun RadioGroup(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Column {
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
}
