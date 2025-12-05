package com.example.badgeuse_auto.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: PresenceViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // All summaries
    val allSummaries by viewModel.allDailySummaries.collectAsState()

    // Settings
    val settings by viewModel.settings.collectAsState(initial = SettingsEntity())
    val dailyWorkMinutes = settings.dailyWorkHours * 60

    // Date filters
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var filtered by remember { mutableStateOf<List<DailyWorkSummary>>(emptyList()) }

    val listToDisplay =
        if (startDate != null && endDate != null && filtered.isNotEmpty())
            filtered
        else
            allSummaries

    // -------------------------
    // CALCULS
    // -------------------------
    val totalMinutesFiltered = listToDisplay.sumOf { it.totalMinutes }
    val totalHoursFiltered = totalMinutesFiltered / 60
    val totalMinFiltered = totalMinutesFiltered % 60

    val weekStart = getStartOfWeek()
    val weekEnd = getEndOfWeek()

    val weeklyMinutes = allSummaries
        .filter { it.dayStart in weekStart..weekEnd }
        .sumOf { it.totalMinutes }

    val weeklyHours = weeklyMinutes / 60
    val weeklyMin = weeklyMinutes % 60

    val weeklySup = (weeklyMinutes - dailyWorkMinutes * 5).coerceAtLeast(0)
    val weeklySupH = weeklySup / 60
    val weeklySupM = weeklySup % 60

    val periodNormal = listToDisplay.size * dailyWorkMinutes
    val periodSup = (totalMinutesFiltered - periodNormal).coerceAtLeast(0)
    val periodSupH = periodSup / 60
    val periodSupM = periodSup % 60

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                SummaryCard(
                    title = "Temps travaillé cette semaine",
                    value = String.format("%02dh %02dmin", weeklyHours, weeklyMin)
                )
            }

            item {
                SummaryCard(
                    title = "Heures supplémentaires (semaine)",
                    value = String.format("%02dh %02dmin", weeklySupH, weeklySupM)
                )
            }

            // -------------------------
            // Filtres rapides
            // -------------------------
            item {
                QuickFilters(
                    onThisWeek = {
                        startDate = Date(weekStart)
                        endDate = Date(weekEnd)
                        scope.launch {
                            filtered = viewModel.loadSummariesBetween(weekStart, weekEnd)
                        }
                    },
                    onThisMonth = {
                        val c = Calendar.getInstance()
                        c.set(Calendar.DAY_OF_MONTH, 1)
                        val start = c.timeInMillis

                        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
                        val end = c.timeInMillis + 86399999L

                        startDate = Date(start)
                        endDate = Date(end)

                        scope.launch { filtered = viewModel.loadSummariesBetween(start, end) }
                    },
                    onLastMonth = {
                        val c = Calendar.getInstance()

                        c.add(Calendar.MONTH, -1)
                        c.set(Calendar.DAY_OF_MONTH, 1)
                        val start = c.timeInMillis

                        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
                        val end = c.timeInMillis + 86399999L

                        startDate = Date(start)
                        endDate = Date(end)

                        scope.launch { filtered = viewModel.loadSummariesBetween(start, end) }
                    }
                )
            }

            // -------------------------
            // Dates (sur la même ligne)
            // -------------------------
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        DatePickerButton(
                            label = "Date de début",
                            date = startDate,
                            onDateSelected = { startDate = it }
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        DatePickerButton(
                            label = "Date de fin",
                            date = endDate,
                            onDateSelected = { endDate = it }
                        )
                    }
                }
            }

            // Bouton filtrer
            item {
                Button(
                    enabled = startDate != null && endDate != null,
                    onClick = {
                        scope.launch {
                            val from = startDate!!.startOfDay()
                            val to = endDate!!.endOfDay()
                            filtered = viewModel.loadSummariesBetween(from, to)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Filtrer") }
            }

            // Totaux période
            item {
                SummaryCard(
                    title = "Total sur la période",
                    value = String.format("%02dh %02dmin", totalHoursFiltered, totalMinFiltered)
                )
            }

            item {
                SummaryCard(
                    title = "Heures supplémentaires (période filtrée)",
                    value = String.format("%02dh %02dmin", periodSupH, periodSupM)
                )
            }

            // Liste des jours
            item {
                SummaryList(listToDisplay)
            }

            item {
                Button(
                    enabled = listToDisplay.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            val file = CsvExportUtils.exportSummariesToCsv(context, listToDisplay)
                            shareCsv(context, file)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Exporter en CSV")
                }
            }
        }
    }

}
private fun getStartOfWeek(): Long {
    val c = Calendar.getInstance()
    c.firstDayOfWeek = Calendar.MONDAY
    c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun getEndOfWeek(): Long {
    val c = Calendar.getInstance()
    c.firstDayOfWeek = Calendar.MONDAY
    c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    c.set(Calendar.HOUR_OF_DAY, 23)
    c.set(Calendar.MINUTE, 59)
    c.set(Calendar.SECOND, 59)
    c.set(Calendar.MILLISECOND, 999)
    return c.timeInMillis
}
@Composable
fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(6.dp))

            Text(
                value,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
@Composable
fun QuickFilters(
    onThisWeek: () -> Unit,
    onThisMonth: () -> Unit,
    onLastMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(onClick = onThisWeek, modifier = Modifier.weight(1f)) {
            Text("Cette semaine")
        }
        Spacer(Modifier.width(8.dp))

        Button(onClick = onThisMonth, modifier = Modifier.weight(1f)) {
            Text("Ce mois")
        }
        Spacer(Modifier.width(8.dp))

        Button(onClick = onLastMonth, modifier = Modifier.weight(1f)) {
            Text("Mois précédent")
        }
    }
}
// -- DatePickerButton (utilise la wrapper PickDateDialog) --
@Composable
fun DatePickerButton(
    label: String,
    date: Date?,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(label, fontWeight = FontWeight.SemiBold)

        OutlinedButton(onClick = { showDialog = true }) {
            Text(date?.let { sdf.format(it) } ?: "Sélectionner")
        }

        if (showDialog) {
            PickDateDialog(
                onDateSelected = {
                    onDateSelected(it)
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}

// -- Wrapper qui utilise Material3 DatePickerDialog --
@OptIn(ExperimentalMaterial3Api::class) // nécessaire pour DatePicker / DatePickerDialog
@Composable
fun PickDateDialog(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    // Utilise le DatePickerDialog Material3 avec les bons paramètres
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(Date(it))
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun Date.startOfDay(): Long {
    val c = Calendar.getInstance()
    c.time = this
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun Date.endOfDay(): Long {
    val c = Calendar.getInstance()
    c.time = this
    c.set(Calendar.HOUR_OF_DAY, 23)
    c.set(Calendar.MINUTE, 59)
    c.set(Calendar.SECOND, 59)
    c.set(Calendar.MILLISECOND, 999)
    return c.timeInMillis
}
@Composable
fun SummaryList(list: List<DailyWorkSummary>) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        list.sortedByDescending { it.dayStart }.forEach { item ->

            val date = sdf.format(Date(item.dayStart))
            val hours = item.totalMinutes / 60
            val minutes = item.totalMinutes % 60

            ElevatedCard(
                Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(date, Modifier.weight(1f))
                    Text(String.format("%02dh%02d", hours, minutes))
                }
            }
        }
    }
}
// ---------------------------------------------------------
// Partage CSV
// ---------------------------------------------------------
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




