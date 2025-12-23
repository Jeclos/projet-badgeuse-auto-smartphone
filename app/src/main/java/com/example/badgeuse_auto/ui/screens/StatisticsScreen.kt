package com.example.badgeuse_auto.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

/* ---------------- ENUM ---------------- */

enum class QuickPeriod(val label: String) {
    THIS_WEEK("Cette semaine"),
    LAST_WEEK("Semaine précédente"),
    THIS_MONTH("Ce mois"),
    LAST_MONTH("Mois précédent")
}

/* ---------------- SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: PresenceViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val allSummaries by viewModel.allDailySummaries.collectAsState()
    val settings by viewModel.settings.collectAsState(initial = SettingsEntity())

    val dailyWorkMinutes = settings.dailyWorkHours * 60

    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var filtered by remember { mutableStateOf<List<DailyWorkSummary>>(emptyList()) }
    var selectedPeriod by remember { mutableStateOf(QuickPeriod.THIS_WEEK) }

    val listToDisplay =
        if (startDate != null && endDate != null) filtered else allSummaries

    val totalMinutes = listToDisplay.sumOf { it.totalMinutes }
    val overtimeMinutes =
        (totalMinutes - listToDisplay.size * dailyWorkMinutes).coerceAtLeast(0)

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

            item {
                SummaryCard(
                    "Temps total",
                    "${totalMinutes / 60}h ${totalMinutes % 60}min"
                )
            }

            item {
                SummaryCard(
                    "Heures supplémentaires",
                    "${overtimeMinutes / 60}h ${overtimeMinutes % 60}min"
                )
            }

            item {
                QuickFilterDropdown(selectedPeriod) {
                    selectedPeriod = it
                    val (s, e) = computePeriod(it)
                    startDate = s
                    endDate = e
                    scope.launch {
                        filtered = viewModel.loadSummariesBetween(s, e)
                    }
                }
            }

            item {
                Text(
                    "Filtre manuel par dates",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                DateRangePickerRow(
                    startDate,
                    endDate,
                    onStartDateSelected = { startDate = it },
                    onEndDateSelected = { endDate = it + 86399999L }
                )
            }

            item {
                SummaryList(listToDisplay)
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = listToDisplay.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            val file = CsvExportUtils.exportSummariesToCsv(context, listToDisplay)
                            shareCsv(context, file)
                        }
                    }
                ) {
                    Text("Exporter CSV")
                }
            }
        }
    }
}

/* ---------------- COMPONENTS ---------------- */

@Composable
fun SummaryCard(title: String, value: String) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SummaryList(items: List<DailyWorkSummary>) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    Column {
        items.forEach {
            Text("${sdf.format(Date(it.dayStart))} – ${it.totalMinutes} min")
        }
    }
}

/* ---------------- DATE PICKER ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerRow(
    startDate: Long?,
    endDate: Long?,
    onStartDateSelected: (Long) -> Unit,
    onEndDateSelected: (Long) -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var showStart by remember { mutableStateOf(false) }
    var showEnd by remember { mutableStateOf(false) }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { showStart = true }) {
            Text(startDate?.let { sdf.format(Date(it)) } ?: "Début")
        }
        OutlinedButton(onClick = { showEnd = true }) {
            Text(endDate?.let { sdf.format(Date(it)) } ?: "Fin")
        }
    }

    if (showStart) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStart = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let(onStartDateSelected)
                    showStart = false
                }) { Text("OK") }
            }
        ) { DatePicker(state) }
    }

    if (showEnd) {
        val state = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEnd = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let(onEndDateSelected)
                    showEnd = false
                }) { Text("OK") }
            }
        ) { DatePicker(state) }
    }
}

/* ---------------- HELPERS ---------------- */

private fun computePeriod(period: QuickPeriod): Pair<Long, Long> {
    val c = Calendar.getInstance()
    return when (period) {
        QuickPeriod.THIS_WEEK -> {
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val s = c.timeInMillis
            c.add(Calendar.DAY_OF_WEEK, 6)
            s to (c.timeInMillis + 86399999L)
        }
        QuickPeriod.LAST_WEEK -> {
            c.add(Calendar.WEEK_OF_YEAR, -1)
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val s = c.timeInMillis
            c.add(Calendar.DAY_OF_WEEK, 6)
            s to (c.timeInMillis + 86399999L)
        }
        QuickPeriod.THIS_MONTH -> {
            c.set(Calendar.DAY_OF_MONTH, 1)
            val s = c.timeInMillis
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            s to (c.timeInMillis + 86399999L)
        }
        QuickPeriod.LAST_MONTH -> {
            c.add(Calendar.MONTH, -1)
            c.set(Calendar.DAY_OF_MONTH, 1)
            val s = c.timeInMillis
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            s to (c.timeInMillis + 86399999L)
        }
    }
}

/* ---------------- CSV ---------------- */

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
