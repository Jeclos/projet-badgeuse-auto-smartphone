package com.example.badgeuse_auto.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.badgeuse_auto.data.PresenceEntry
import com.example.badgeuse_auto.data.PresenceViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*




@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun MainScreen(
    viewModel: PresenceViewModel,
    onNavigateStats: () -> Unit,
    onNavigateWorkLocation: () -> Unit,
    onNavigateSettings: () -> Unit,


){

    val presences by viewModel.allPresences.collectAsState()
    val clock = remember { mutableStateOf(System.currentTimeMillis()) }

    // Dialog d’édition
    var entryToEdit by remember { mutableStateOf<PresenceEntry?>(null) }

    // --- Clock update every second ---
    LaunchedEffect(Unit) {
        while (true) {
            clock.value = System.currentTimeMillis()
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Badgeuse Auto") },
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuration"
                        )
                    }

                    IconButton(onClick = onNavigateStats) {
                        Icon(
                            imageVector = Icons.Filled.Leaderboard,
                            contentDescription = "Statistiques"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- Clock display ---
            Text(
                text = "Heure : ${formatClock(clock.value)}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Location dropdown ---
            var selectedLocation by remember { mutableStateOf("Bureau") }
            var expanded by remember { mutableStateOf(false) }
            val locations = listOf("Bureau", "Maison", "Client")

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = selectedLocation,
                    onValueChange = {},
                    label = { Text("Lieu") },
                    readOnly = true,
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    locations.forEach { loc ->
                        DropdownMenuItem(
                            text = { Text(loc) },
                            onClick = {
                                selectedLocation = loc
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Entry / Exit buttons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Button(onClick = {
                    viewModel.manualEvent("ENTREE") { _, _ -> }
                }) {
                    Text("Entrée")
                }

                Button(onClick = {
                    viewModel.manualEvent("SORTIE") { _, _ -> }
                }) {
                    Text("Sortie")
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            // --- NEW BUTTON: Work Location ---
            Button(
                onClick = onNavigateWorkLocation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lieu de travail")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Title ---
            Text(
                text = "Historique des présences",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- History list ---
            LazyColumn {
                items(presences) { entry ->
                    PresenceCard(
                        entry = entry,
                        onEdit = { entryToEdit = it },
                        onDelete = { viewModel.deletePresence(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // --- DIALOG EDITION ---
    entryToEdit?.let { entry ->
        EditPresenceDialog(
            entry = entry,
            onDismiss = { entryToEdit = null },
            onValidate = { updated ->
                viewModel.updatePresence(updated)
                entryToEdit = null
            }
        )
    }
}



// ---------------------------------------------------------------------
// Carte avec menu Modifier / Supprimer
// ---------------------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PresenceCard(
    entry: PresenceEntry,
    onEdit: (PresenceEntry) -> Unit,
    onDelete: (PresenceEntry) -> Unit
) {

    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { menuExpanded = true }
            ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Menu contextuel
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {

                DropdownMenuItem(
                    text = { Text("Modifier") },
                    onClick = {
                        menuExpanded = false
                        onEdit(entry)
                    }
                )

                DropdownMenuItem(
                    text = { Text("Supprimer") },
                    onClick = {
                        menuExpanded = false
                        onDelete(entry)
                    }
                )
            }

            Text(
                text = "${entry.type} - ${formatDate(entry.timestamp)}",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Lieu : ${entry.locationName}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}



// ---------------------------------------------------------------------
// Dialog d’édition d’une présence
// ---------------------------------------------------------------------
@Composable
fun EditPresenceDialog(
    entry: PresenceEntry,
    onDismiss: () -> Unit,
    onValidate: (PresenceEntry) -> Unit
) {

    var type by remember { mutableStateOf(entry.type) }
    var location by remember { mutableStateOf(entry.locationName) }
    var timestampStr by remember { mutableStateOf(formatDate(entry.timestamp)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier la présence") },

        text = {
            Column {

                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type (ENTREE / SORTIE)") }
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lieu") }
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = timestampStr,
                    onValueChange = { timestampStr = it },
                    label = { Text("Date (dd/MM/yyyy HH:mm)") }
                )
            }
        },

        confirmButton = {
            TextButton(onClick = {
                val newTs = try {
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .parse(timestampStr)?.time ?: entry.timestamp
                } catch (e: Exception) {
                    entry.timestamp
                }

                onValidate(
                    entry.copy(
                        type = type,
                        locationName = location,
                        timestamp = newTs
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
}



// ---------------------------------------------------------------------
// Utils
// ---------------------------------------------------------------------
fun formatDate(ts: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(ts))
}

fun formatClock(ts: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(ts))
}
