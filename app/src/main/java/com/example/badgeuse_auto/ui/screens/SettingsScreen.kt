package com.example.badgeuse_auto.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import com.example.badgeuse_auto.ui.components.TimeRangePicker
import com.example.badgeuse_auto.ui.components.WallpaperBackground
import com.example.badgeuse_auto.ui.help.BadgeHelp
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
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val fusedLocationClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }

    val settings by settingsViewModel.settingsFlow.collectAsState(SettingsEntity())
    val workLocations by presenceViewModel.allWorkLocations.collectAsState()

    /* ================= UI STATES ================= */

    var enterDistance by remember { mutableStateOf(0) }
    var exitDistance by remember { mutableStateOf(0) }
    var enterDelay by remember { mutableStateOf(0) }
    var exitDelay by remember { mutableStateOf(0) }

    var lunchEnabled by remember { mutableStateOf(false) }
    var lunchOutside by remember { mutableStateOf(true) }
    var lunchDuration by remember { mutableStateOf(0) }

    var lunchWindowStartHour by remember { mutableStateOf(12) }
    var lunchWindowStartMinute by remember { mutableStateOf(0) }
    var lunchWindowEndHour by remember { mutableStateOf(14) }
    var lunchWindowEndMinute by remember { mutableStateOf(0) }

    var lunchMinDurationMin by remember { mutableStateOf(0) }
    var lunchDefaultDurationMin by remember { mutableStateOf(0) }

    var travelTime by remember { mutableStateOf(0) }

    var depotStartHour by remember { mutableStateOf(0) }
    var depotStartMinute by remember { mutableStateOf(0) }
    var depotEndHour by remember { mutableStateOf(0) }
    var depotEndMinute by remember { mutableStateOf(0) }
    var depotAdjust by remember { mutableStateOf(0) }

    var employeeName by remember { mutableStateOf("") }
    var employeeAddress by remember { mutableStateOf("") }
    var employerName by remember { mutableStateOf("") }
    var employerAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    var newWorkName by remember { mutableStateOf("") }
    var newWorkLat by remember { mutableStateOf("") }
    var newWorkLon by remember { mutableStateOf("") }

    var editedLocation by remember { mutableStateOf<WorkLocationEntity?>(null) }
    var helpTitle by remember { mutableStateOf("") }
    var helpText by remember { mutableStateOf<String?>(null) }

    val hasLocationPermission =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    /* ================= LOAD SETTINGS ================= */

    LaunchedEffect(settings) {
        enterDistance = settings.enterDistance
        exitDistance = settings.exitDistance
        enterDelay = settings.enterDelaySec
        exitDelay = settings.exitDelaySec

        lunchEnabled = settings.lunchBreakEnabled
        lunchOutside = settings.lunchBreakOutside
        lunchDuration = settings.lunchBreakDurationMin

        lunchWindowStartHour = settings.lunchWindowStartHour
        lunchWindowStartMinute = settings.lunchWindowStartMinute
        lunchWindowEndHour = settings.lunchWindowEndHour
        lunchWindowEndMinute = settings.lunchWindowEndMinute

        lunchMinDurationMin = settings.lunchMinDurationMin
        lunchDefaultDurationMin = settings.lunchDefaultDurationMin

        travelTime = settings.travelTimeMin

        depotStartHour = settings.depotStartHour
        depotStartMinute = settings.depotStartMinute
        depotEndHour = settings.depotEndHour
        depotEndMinute = settings.depotEndMinute
        depotAdjust = settings.depotDailyAdjustMin

        employeeName = settings.employeeName
        employeeAddress = settings.employeeAddress
        employerName = settings.employerName
        employerAddress = settings.employerAddress
        city = settings.city
    }

    /* ================= SCAFFOLD ================= */

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
                            enterDistance,
                            exitDistance,
                            enterDelay,
                            exitDelay,
                            lunchEnabled,
                            lunchOutside,
                            lunchDuration,
                            lunchWindowStartHour,
                            lunchWindowStartMinute,
                            lunchWindowEndHour,
                            lunchWindowEndMinute,
                            lunchMinDurationMin,
                            lunchDefaultDurationMin,
                            employeeName,
                            employeeAddress,
                            employerName,
                            employerAddress,
                            city,
                            depotStartHour,
                            depotStartMinute,
                            depotEndHour,
                            depotEndMinute,
                            depotAdjust,
                            travelTime
                        ) { onBack() }
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

                /* ================= DÉTECTION ================= */

                SettingsCard(Icons.Default.LocationOn, "Détection") {

                    NumberField(
                        label = "Distance entrée (m)",
                        value = enterDistance,
                        range = 0..1000
                    ) { enterDistance = it }

                    NumberField(
                        label = "Distance sortie (m)",
                        value = exitDistance,
                        range = 0..1000
                    ) { exitDistance = it }
                }

                /* ================= TEMPORISATION ================= */

                SettingsCard(Icons.Default.Schedule, "Temporisation") {

                    NumberField(
                        label = "Entrée (sec)",
                        value = enterDelay,
                        range = 0..1000
                    ) { enterDelay = it }

                    NumberField(
                        label = "Sortie (sec)",
                        value = exitDelay,
                        range = 0..1000
                    ) { exitDelay = it }
                }
            }
        }
    }

    helpText?.let {
        InfoDialog(
            title = helpTitle,
            text = it,
            onDismiss = { helpText = null }
        )
    }


    editedLocation?.let { loc ->
        EditWorkLocationDialog(
            location = loc,
            onDismiss = { editedLocation = null },
            onConfirm = { name, lat, lon ->
                presenceViewModel.updateWorkLocation(
                    location = loc,
                    name = name,
                    latitude = lat,
                    longitude = lon
                )
                editedLocation = null
            }
        )
    }

}

/* ======================= HELPERS ========================== */

@Composable
fun SettingsCard(
    icon: ImageVector,
    title: String,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null)
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null
                )
            }

            AnimatedVisibility(expanded) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun NumberField(
    label: String,
    value: Int,
    range: IntRange = Int.MIN_VALUE..Int.MAX_VALUE,
    onValueChange: (Int) -> Unit
) {
    var text by remember { mutableStateOf(value.toString()) }

    LaunchedEffect(value) {
        val newValue = value.toString()
        if (text != newValue) {
            text = newValue
        }
    }

    OutlinedTextField(
        label = { Text(label) },
        value = text,
        onValueChange = {
            text = it
            it.toIntOrNull()?.let { v ->
                if (v in range) onValueChange(v)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
@Composable
fun EditWorkLocationDialog(
    location: WorkLocationEntity,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf(location.name) }
    var lat by remember { mutableStateOf(location.latitude.toString()) }
    var lon by remember { mutableStateOf(location.longitude.toString()) }

    val context = LocalContext.current
    val fusedLocationClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }

    val hasLocationPermission =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Modifier le lieu")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") }
                )

                OutlinedTextField(
                    value = lat,
                    onValueChange = { lat = it },
                    label = { Text("Latitude") }
                )

                OutlinedTextField(
                    value = lon,
                    onValueChange = { lon = it },
                    label = { Text("Longitude") }
                )

                OutlinedButton(
                    enabled = hasLocationPermission,
                    onClick = {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                lat = it.latitude.toString()
                                lon = it.longitude.toString()
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Utiliser ma position actuelle")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        name,
                        lat.toDoubleOrNull() ?: location.latitude,
                        lon.toDoubleOrNull() ?: location.longitude
                    )
                }
            ) {
                Text("Valider")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
@Composable
fun InfoDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(text)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

