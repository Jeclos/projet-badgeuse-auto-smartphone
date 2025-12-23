package com.example.badgeuse_auto.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.badgeuse_auto.data.DailyWorkSummary
import com.example.badgeuse_auto.data.PresenceViewModel
import com.example.badgeuse_auto.data.SettingsEntity
import com.example.badgeuse_auto.utils.CsvExportUtils
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/* ---------------- SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: PresenceViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val allSummaries by viewModel.allDailySummaries.collectAsState()
    val settings by viewModel.settings.collectAsState(initial = SettingsEntity())

    val dailyWorkMinutes = settings.dailyWorkHours * 60

    var selectedPeriod by remember { mutableStateOf(QuickPeriod.THIS_WEEK) }
    var filtered by remember { mutableStateOf<List<DailyWorkSummary>>(emptyList()) }

    LaunchedEffect(selectedPeriod) {
        val (s, e) = computePeriod(selectedPeriod)
        filtered = viewModel.loadSummariesBetween(s, e)
    }

    val totalMinutes = filtered.sumOf { it.totalMinutes }
    val overtimeMinutes =
        (totalMinutes - filtered.size * dailyWorkMinutes).coerceAtLeast(0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /* ---------- KPI ---------- */

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "Temps total",
                        value = formatMinutes(totalMinutes),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Heures sup.",
                        value = formatMinutes(overtimeMinutes),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            /* ---------- FILTER ---------- */

            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Période",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))
                        QuickFilterDropdown(
                            selectedPeriod = selectedPeriod,
                            onPeriodSelected = { selectedPeriod = it }
                        )
                    }
                }
            }

            /* ---------- LIST ---------- */

            item {
                Text(
                    "Détail journalier",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            items(filtered) { summary ->
                DaySummaryCard(summary)
            }

            /* ---------- EXPORT ---------- */

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = filtered.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            val file = CsvExportUtils.exportSummariesToCsv(context, filtered)
                            shareCsv(context, file)
                        }
                    }
                ) {
                    Icon(Icons.Default.FileDownload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Exporter en CSV")
                }
            }
        }
    }
}

/* ---------------- UI COMPONENTS ---------------- */

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DaySummaryCard(summary: DailyWorkSummary) {
    val sdf = remember { SimpleDateFormat("EEEE dd/MM", Locale.getDefault()) }

    Card {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    sdf.format(Date(summary.dayStart)),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Temps travaillé",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                formatMinutes(summary.totalMinutes),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/* ---------------- HELPERS ---------------- */

private fun formatMinutes(minutes: Int): String =
    "${minutes / 60}h ${minutes % 60}min"

private fun shareCsv(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Partager CSV"))
}
enum class QuickPeriod(val label: String) {
    THIS_WEEK("Cette semaine"),
    LAST_WEEK("Semaine précédente"),
    THIS_MONTH("Ce mois"),
    LAST_MONTH("Mois précédent")
}
private fun computePeriod(period: QuickPeriod): Pair<Long, Long> {
    val c = Calendar.getInstance()

    return when (period) {
        QuickPeriod.THIS_WEEK -> {
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val start = c.timeInMillis
            c.add(Calendar.DAY_OF_WEEK, 6)
            start to (c.timeInMillis + 86_399_999L)
        }

        QuickPeriod.LAST_WEEK -> {
            c.add(Calendar.WEEK_OF_YEAR, -1)
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val start = c.timeInMillis
            c.add(Calendar.DAY_OF_WEEK, 6)
            start to (c.timeInMillis + 86_399_999L)
        }

        QuickPeriod.THIS_MONTH -> {
            c.set(Calendar.DAY_OF_MONTH, 1)
            val start = c.timeInMillis
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            start to (c.timeInMillis + 86_399_999L)
        }

        QuickPeriod.LAST_MONTH -> {
            c.add(Calendar.MONTH, -1)
            c.set(Calendar.DAY_OF_MONTH, 1)
            val start = c.timeInMillis
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            start to (c.timeInMillis + 86_399_999L)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickFilterDropdown(
    selectedPeriod: QuickPeriod,
    onPeriodSelected: (QuickPeriod) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedPeriod.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Période") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            QuickPeriod.values().forEach { period ->
                DropdownMenuItem(
                    text = { Text(period.label) },
                    onClick = {
                        expanded = false
                        onPeriodSelected(period)
                    }
                )
            }
        }
    }
}
