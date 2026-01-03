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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable


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
    var travelTime by remember { mutableStateOf("") }


    /* ================= UI STATES ================= */

    var enterDistance by remember { mutableStateOf("") }
    var exitDistance by remember { mutableStateOf("") }
    var enterDelay by remember { mutableStateOf("") }
    var exitDelay by remember { mutableStateOf("") }

    var lunchEnabled by remember { mutableStateOf(false) }
    var lunchOutside by remember { mutableStateOf(true) }
    var lunchDuration by remember { mutableStateOf("") }

    var employeeName by remember { mutableStateOf("") }
    var employeeAddress by remember { mutableStateOf("") }
    var employerName by remember { mutableStateOf("") }
    var employerAddress by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    var depotStartHour by remember { mutableStateOf("") }
    var depotStartMinute by remember { mutableStateOf("") }
    var depotEndHour by remember { mutableStateOf("") }
    var depotEndMinute by remember { mutableStateOf("") }
    var depotAdjust by remember { mutableStateOf("") }

    var newWorkName by remember { mutableStateOf("") }
    var newWorkLat by remember { mutableStateOf("") }
    var newWorkLon by remember { mutableStateOf("") }

    var editedLocation by remember { mutableStateOf<WorkLocationEntity?>(null) }

    val hasLocationPermission =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    /* ================= LOAD SETTINGS ================= */

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

        depotStartHour = settings.depotStartHour.toString()
        depotStartMinute = settings.depotStartMinute.toString()
        depotEndHour = settings.depotEndHour.toString()
        depotEndMinute = settings.depotEndMinute.toString()
        depotAdjust = settings.depotDailyAdjustMin.toString()
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
                            city,
                            depotStartHour.toIntOrNull() ?: settings.depotStartHour,
                            depotStartMinute.toIntOrNull() ?: settings.depotStartMinute,
                            depotEndHour.toIntOrNull() ?: settings.depotEndHour,
                            depotEndMinute.toIntOrNull() ?: settings.depotEndMinute,
                            depotAdjust.toIntOrNull() ?: settings.depotDailyAdjustMin,
                            travelTime.toIntOrNull() ?: settings.travelTimeMin,
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

                /* ================= IDENTITÉ ================= */

                SettingsCard(Icons.Default.Badge, "Identité") {
                    OutlinedTextField(employeeName, { employeeName = it }, label = { Text("Nom employé") })
                    OutlinedTextField(employeeAddress, { employeeAddress = it }, label = { Text("Adresse employé") })
                    Divider()
                    OutlinedTextField(employerName, { employerName = it }, label = { Text("Nom employeur") })
                    OutlinedTextField(employerAddress, { employerAddress = it }, label = { Text("Adresse employeur") })
                    OutlinedTextField(city, { city = it }, label = { Text("Ville (PDF)") })
                }

                /* ================= MODE ================= */

                SettingsCard(Icons.Default.Tune, "Mode de badgeage") {
                    BadgeMode.values().forEach { mode ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = settings.badgeMode == mode,
                                onClick = {
                                    settingsViewModel.setBadgeMode(mode)
                                    presenceViewModel.onBadgeModeChanged(mode)
                                }
                            )

                            Text(
                                when (mode) {
                                    BadgeMode.OFFICE -> "Bureau / Multi-lieux"
                                    BadgeMode.DEPOT -> "Dépôt / Entrepôt"
                                    BadgeMode.HOME_TRAVEL -> "Départ domicile"
                                    BadgeMode.MANUAL_ONLY -> "Manuel uniquement"
                                }
                            )
                        }
                    }
                }


                    /* ================= DÉTECTION ================= */

                SettingsCard(Icons.Default.LocationOn, "Détection") {
                    NumberField("Distance entrée (m)", enterDistance) { enterDistance = it }
                    NumberField("Distance sortie (m)", exitDistance) { exitDistance = it }
                }

                /* ================= TEMPORISATION ================= */

                SettingsCard(Icons.Default.Schedule, "Temporisation") {
                    NumberField("Entrée (sec)", enterDelay) { enterDelay = it }
                    NumberField("Sortie (sec)", exitDelay) { exitDelay = it }
                }

                /* ========== PARAMETRES MODE HOME_TRAVEL ========= */

                if (settings.badgeMode == BadgeMode.HOME_TRAVEL) {
                    SettingsCard(Icons.Default.DirectionsCar, "Temps de trajet") {
                        NumberField(
                            label = "Durée du trajet (min)",
                            value = travelTime
                        ) {
                            travelTime = it
                        }

                        Text(
                            "Ce temps est soustrait au départ et ajouté au retour",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }




                /* ================= PAUSE DÉJEUNER ================= */

                SettingsCard(Icons.Default.LunchDining, "Pause déjeuner") {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Activer la pause", Modifier.weight(1f))
                        Switch(checked = lunchEnabled, onCheckedChange = { lunchEnabled = it })
                    }

                    if (lunchEnabled) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = lunchOutside, onClick = { lunchOutside = true })
                            Text("À l'extérieur")
                            Spacer(Modifier.width(16.dp))
                            RadioButton(selected = !lunchOutside, onClick = { lunchOutside = false })
                            Text("Sur place")
                        }

                        NumberField("Durée (min)", lunchDuration) { lunchDuration = it }
                    }
                }

                /* ================= DÉPÔT ================= */

                if (settings.badgeMode == BadgeMode.DEPOT) {
                    SettingsCard(Icons.Default.Warehouse, "Paramètres du dépôt") {

                        Text("Heures officielles", fontWeight = FontWeight.Medium)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NumberField("Début (h)", depotStartHour, Modifier.weight(1f)) {
                                depotStartHour = it
                            }
                            NumberField("Début (min)", depotStartMinute, Modifier.weight(1f)) {
                                depotStartMinute = it
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NumberField("Fin (h)", depotEndHour, Modifier.weight(1f)) {
                                depotEndHour = it
                            }
                            NumberField("Fin (min)", depotEndMinute, Modifier.weight(1f)) {
                                depotEndMinute = it
                            }
                        }

                        Divider()

                        NumberField("Ajustement journalier (min)", depotAdjust) {
                            depotAdjust = it
                        }

                        Text(
                            "Ex: -15 = départ anticipé, +10 = dépassement",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                /* ================= LIEUX ================= */

                SettingsCard(Icons.Default.Work, "Lieux de travail") {

                    val isDepotMode = settings.badgeMode == BadgeMode.DEPOT

                    workLocations.forEach { loc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = loc.name,
                                    fontWeight = FontWeight.Medium,
                                    color = if (loc.isActive)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )

                                Text(
                                    "${loc.latitude}, ${loc.longitude}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // ✅ ACTIVE / DÉSACTIVE (NE SUPPRIME JAMAIS)
                            Switch(
                                checked = loc.isActive,
                                onCheckedChange = { isActive ->
                                    presenceViewModel.setWorkLocationActive(loc, isActive)
                                }
                            )

                            IconButton(onClick = { editedLocation = loc }) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifier")
                            }

                            // ❌ SUPPRESSION EXPLICITE UNIQUEMENT
                            IconButton(onClick = { presenceViewModel.deleteWorkLocation(loc) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Divider()
                    }


                    // ➕ AJOUT AUTORISÉ
                    OutlinedTextField(
                        value = newWorkName,
                        onValueChange = { newWorkName = it },
                        label = { Text("Nom du lieu") }
                    )

                    Row {
                        OutlinedTextField(
                            value = newWorkLat,
                            onValueChange = { newWorkLat = it },
                            label = { Text("Latitude") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = newWorkLon,
                            onValueChange = { newWorkLon = it },
                            label = { Text("Longitude") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = hasLocationPermission,
                        onClick = {
                            fusedLocationClient.lastLocation.addOnSuccessListener {
                                it?.let {
                                    newWorkLat = it.latitude.toString()
                                    newWorkLon = it.longitude.toString()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.MyLocation, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Utiliser ma position actuelle")
                    }

                    Button(
                        enabled =
                            newWorkName.isNotBlank() &&
                                    newWorkLat.toDoubleOrNull() != null &&
                                    newWorkLon.toDoubleOrNull() != null,
                        onClick = {
                            presenceViewModel.addWorkLocation(
                                newWorkName,
                                newWorkLat.toDouble(),
                                newWorkLon.toDouble()
                            )
                            newWorkName = ""
                            newWorkLat = ""
                            newWorkLon = ""
                        }
                    ) {
                        Text("Ajouter le lieu")
                    }
                }

                /* ================= APPARENCE ================= */

                SettingsCard(Icons.Default.Palette, "Apparence") {

                    /* -------- STYLE -------- */

                    Text("Style", fontWeight = FontWeight.Medium)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppStyle.values().forEach { style ->
                            FilterChip(
                                selected = settings.appStyle == style.name,
                                onClick = { settingsViewModel.setAppStyle(style) },
                                label = { Text(style.name) }
                            )
                        }
                    }

                    Divider()

                    /* -------- THÈME -------- */

                    Text("Thème", fontWeight = FontWeight.Medium)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        ThemeMode.values().forEach { mode ->

                            val icon = when (mode) {
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                ThemeMode.DARK -> Icons.Default.DarkMode
                                ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                            }

                            FilledIconToggleButton(
                                checked = settings.themeMode == mode,
                                onCheckedChange = {
                                    settingsViewModel.setThemeMode(mode)
                                }
                            ) {
                                Icon(icon, contentDescription = mode.name)
                            }
                        }

                    }
                }

            }
        }
    }

    editedLocation?.let { loc ->
        EditWorkLocationDialog(
            location = loc,
            onDismiss = { editedLocation = null },
            onConfirm = { name, lat, lon ->
                presenceViewModel.updateWorkLocation(loc, name, lat, lon)
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
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column {

            // HEADER (toujours visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded)
                        Icons.Default.ExpandLess
                    else
                        Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            // CONTENU REPLIABLE
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
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
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.all(Char::isDigit) || it.startsWith("-")) onValueChange(it) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth()
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
        title = { Text("Modifier le lieu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nom") })
                OutlinedTextField(lat, { lat = it }, label = { Text("Latitude") })
                OutlinedTextField(lon, { lon = it }, label = { Text("Longitude") })

                OutlinedButton(
                    enabled = hasLocationPermission,
                    onClick = {
                        fusedLocationClient.lastLocation.addOnSuccessListener {
                            it?.let {
                                lat = it.latitude.toString()
                                lon = it.longitude.toString()
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.MyLocation, null)
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
        }
,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
