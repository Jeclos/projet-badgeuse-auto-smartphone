package com.example.badgeuse_auto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
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
    onNavigateSettings: () -> Unit
){

    val presences by viewModel.allPresences.collectAsState()
    val clock = remember { mutableStateOf(System.currentTimeMillis()) }

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
                    IconButton(onClick = onNavigateStats) {
                        Icon(
                            imageVector = Icons.Filled.Leaderboard,
                            contentDescription = "Stats"
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
                    viewModel.manualEvent("ENTREE") { success, message ->
                        // afficher le message dans un Toast ou Snackbar
                    }

                }) {
                    Text("Entrée")
                }
                Button(onClick = onNavigateSettings) {
                    Text("Configuration")
                }

                Button(onClick = {
                    viewModel.manualEvent("SORTIE") { success, message ->
                        // ...
                    }

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
                    PresenceCard(entry)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun PresenceCard(entry: PresenceEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

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

// --- Utils ---
fun formatDate(ts: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(ts))
}

fun formatClock(ts: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(ts))
}
