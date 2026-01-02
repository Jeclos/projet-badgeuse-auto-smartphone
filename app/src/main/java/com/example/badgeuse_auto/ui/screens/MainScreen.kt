package com.example.badgeuse_auto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.badgeuse_auto.data.PresenceEntity
import com.example.badgeuse_auto.data.PresenceViewModel
import com.example.badgeuse_auto.ui.components.*
import com.example.badgeuse_auto.ui.location.LocationViewModel
import com.example.badgeuse_auto.ui.utils.formatClock
import com.example.badgeuse_auto.ui.utils.formatMinutes
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PresenceViewModel,
    locationViewModel: LocationViewModel,
    onNavigateStats: () -> Unit,
    onNavigateSettings: () -> Unit
) {

    /* ---------------- STATE ---------------- */

    val presences by viewModel.allPresences.collectAsState()
    val workLocations by viewModel.allWorkLocations.collectAsState()
    val locationUi by locationViewModel.location.collectAsState()

    val locationMap = remember(workLocations) {
        workLocations.associate { it.id to it.name }
    }

    val totalMinutesToday by viewModel
        .totalMinutesToday()
        .collectAsState(initial = 0)

    val activeLocations = workLocations.filter { it.isActive }

    var clock by remember { mutableStateOf(System.currentTimeMillis()) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<PresenceEntity?>(null) }

    /* ---------------- GPS ---------------- */

    LaunchedEffect(Unit) {
        locationViewModel.startLocationUpdates()
    }

    DisposableEffect(Unit) {
        onDispose { locationViewModel.stopLocationUpdates() }
    }

    /* ---------------- CLOCK ---------------- */

    LaunchedEffect(Unit) {
        while (true) {
            clock = System.currentTimeMillis()
            delay(1_000)
        }
    }

    /* ---------------- WORK STATE ---------------- */

    val lastPresence = presences.firstOrNull()
    val isWorking = lastPresence != null && lastPresence.exitTime == null

    val currentLocationName =
        if (isWorking && lastPresence != null) {
            locationMap[lastPresence.workLocationId] ?: "Lieu inconnu"
        } else {
            "Hors travail"
        }

    /* ---------------- UI ---------------- */

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Badgeuse Auto") },
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, null)
                    }
                    IconButton(onClick = onNavigateStats) {
                        Icon(Icons.Default.Leaderboard, null)
                    }
                }
            )
        }
    ) { padding ->

        AppBackground {

            // 🕰️ HORLOGE FLOTANTE
            FloatingIllustratedClock()

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                /* -------- STATUS -------- */

                WorkStatusBar(
                    isWorking = isWorking,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                /* -------- CARTE RÉCAP + GPS -------- */

                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        /* ---- INFOS TRAVAIL ---- */
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                formatClock(clock),
                                style = MaterialTheme.typography.headlineMedium
                            )

                            Text(
                                if (isWorking) "En cours de travail" else "Hors travail",
                                color = if (isWorking)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )

                            Text("Temps aujourd’hui : ${formatMinutes(totalMinutesToday)}")

                            Text(
                                "Lieu : $currentLocationName",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        /* ---- GPS COMPACT ---- */
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {

                            val gpsIcon = when {
                                locationUi.error != null ->
                                    Icons.Outlined.LocationOff
                                locationUi.latitude != null ->
                                    Icons.Outlined.GpsFixed
                                else ->
                                    Icons.Outlined.GpsNotFixed
                            }

                            val gpsColor = when {
                                locationUi.error != null ->
                                    MaterialTheme.colorScheme.error
                                locationUi.accuracy != null && locationUi.accuracy!! <= 30 ->
                                    MaterialTheme.colorScheme.primary
                                else ->
                                    MaterialTheme.colorScheme.tertiary
                            }

                            Icon(
                                gpsIcon,
                                contentDescription = null,
                                tint = gpsColor,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                when {
                                    locationUi.error != null -> "GPS off"
                                    locationUi.latitude != null -> "GPS ok"
                                    else -> "GPS…"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )

                            locationUi.accuracy?.let {
                                Text(
                                    "± ${it.toInt()} m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = gpsColor
                                )
                            }
                        }
                    }
                }

                /* -------- ACTIONS -------- */

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        12.dp,
                        Alignment.CenterHorizontally
                    )
                ) {

                    OutlinedButton(
                        enabled = !isWorking,
                        onClick = {
                            when {
                                activeLocations.isEmpty() -> Unit
                                activeLocations.size == 1 ->
                                    viewModel.manualEntry(activeLocations.first().id)
                                else -> showLocationPicker = true
                            }
                        }
                    ) {
                        Icon(Icons.Default.Login, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Entrée")
                    }

                    OutlinedButton(
                        enabled = isWorking,
                        onClick = { viewModel.manualExit() }
                    ) {
                        Icon(Icons.Default.Logout, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Sortie")
                    }
                }

                /* -------- PRESENCE LIST -------- */

                LazyColumn {
                    items(presences) { entry ->
                        PresenceCard(
                            entry = entry,
                            locationName = locationMap[entry.workLocationId]
                                ?: "Lieu inconnu",
                            onEdit = { entryToEdit = it },
                            onDelete = { viewModel.deletePresence(it) }
                        )
                    }
                }
            }
        }
    }

    /* ---------------- LOCATION PICKER ---------------- */

    if (showLocationPicker) {
        AlertDialog(
            onDismissRequest = { showLocationPicker = false },
            title = { Text("Choisir le lieu") },
            text = {
                Column {
                    activeLocations.forEach { loc ->
                        TextButton(
                            onClick = {
                                viewModel.manualEntry(loc.id)
                                showLocationPicker = false
                            }
                        ) {
                            Icon(Icons.Outlined.Work, null)
                            Spacer(Modifier.width(8.dp))
                            Text(loc.name)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLocationPicker = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    /* ---------------- EDIT ---------------- */

    entryToEdit?.let {
        EditPresenceDialog(
            entry = it,
            onDismiss = { entryToEdit = null },
            onValidate = {
                viewModel.updatePresence(it)
                entryToEdit = null
            }
        )
    }
}
