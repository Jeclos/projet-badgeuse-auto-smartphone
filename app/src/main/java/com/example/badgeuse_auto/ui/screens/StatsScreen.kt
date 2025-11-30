package com.example.badgeuse_auto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.badgeuse_auto.data.PresenceViewModel
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: PresenceViewModel, onBack: () -> Unit) {
    val totalTodayFlow = viewModel.totalMinutesToday()
    val totalToday by totalTodayFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Temps total aujourd'hui", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))
            Text("$totalToday minutes", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            // More widgets could be placed here: weekly summary, graphs, etc.
            Text("Dernières entrées/sorties sont visibles sur l'écran principal.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
