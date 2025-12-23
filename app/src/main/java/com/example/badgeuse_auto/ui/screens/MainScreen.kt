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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.AccessTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PresenceViewModel,
    onNavigateStats: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val presences by viewModel.allPresences.collectAsState(initial = emptyList())
    val workLocation by viewModel.workLocation.collectAsState(initial = null)
    val clock = remember { mutableStateOf(System.currentTimeMillis()) }

    var entryToEdit by remember { mutableStateOf<PresenceEntry?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            clock.value = System.currentTimeMillis()
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Badgeuse Auto",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuration")
                    }
                    IconButton(onClick = onNavigateStats) {
                        Icon(Icons.Filled.Leaderboard, contentDescription = "Statistiques")
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

            Text(
                text = "Heure : ${formatClock(clock.value)}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Lieu de travail : ${workLocation?.name ?: "Non configuré"}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { viewModel.manualEvent("ENTREE") { _, _ -> } }) {
                    Text("Entrée")
                }
                Button(onClick = { viewModel.manualEvent("SORTIE") { _, _ -> } }) {
                    Text("Sortie")
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Historique des présences",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn {
                items(presences) { entry ->
                    PresenceCard(
                        entry = entry,
                        onEdit = { entryToEdit = it },
                        onDelete = { viewModel.deletePresence(it) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    entryToEdit?.let { entry ->
        EditPresenceDialog(
            entry = entry,
            onDismiss = { entryToEdit = null },
            onValidate = {
                viewModel.updatePresence(it)
                entryToEdit = null
            }
        )
    }
}
private fun formatClock(timeMillis: Long): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timeMillis))
}
private fun formatDate(timeMillis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timeMillis))
}

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
        Box {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "${entry.type} - ${formatDate(entry.timestamp)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary

                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Lieu : ${entry.locationName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

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
        }
    }
}
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
                    label = { Text("Type (ENTREE / SORTIE)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lieu") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = timestampStr,
                    onValueChange = { timestampStr = it },
                    label = { Text("Date (dd/MM/yyyy HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newTimestamp = try {
                    SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                    ).parse(timestampStr)?.time ?: entry.timestamp
                } catch (e: Exception) {
                    entry.timestamp
                }

                onValidate(
                    entry.copy(
                        type = type,
                        locationName = location,
                        timestamp = newTimestamp
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
