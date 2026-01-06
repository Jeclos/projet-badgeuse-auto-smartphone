package com.example.badgeuse_auto.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
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
import com.example.badgeuse_auto.data.PresenceViewModel
import com.example.badgeuse_auto.data.SettingsEntity
import com.example.badgeuse_auto.utils.CsvExportUtils
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.example.badgeuse_auto.ui.utils.formatMinutes
import com.example.badgeuse_auto.data.DailyStat
import com.example.badgeuse_auto.export.ExportHeader
import androidx.compose.material.icons.filled.PictureAsPdf
import com.example.badgeuse_auto.export.PdfExportUtils
import kotlinx.coroutines.flow.map





/* ---------------- SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: PresenceViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ SETTINGS (UNE SEULE FOIS)
    val settings by viewModel.settings.collectAsState()

    val locationNames by viewModel
        .allWorkLocations
        .map { locations -> locations.map { it.name }.distinct().sorted() }
        .collectAsState(initial = emptyList())


    val dailyWorkMinutes = settings.dailyWorkHours * 60

    var selectedPeriod by remember { mutableStateOf(QuickPeriod.THIS_WEEK) }
    var customStart by remember { mutableStateOf<Long?>(null) }
    var customEnd by remember { mutableStateOf<Long?>(null) }
    var selectedLocation by remember { mutableStateOf<String?>(null) }

    // ✅ PÉRIODE (AVANT EXPORT)
    val (start, end) = remember(selectedPeriod, customStart, customEnd) {
        when (selectedPeriod) {
            QuickPeriod.CUSTOM ->
                if (customStart != null && customEnd != null)
                    customStart!! to customEnd!!
                else 0L to 0L

            else -> computePeriod(selectedPeriod)
        }
    }
    val isPeriodValid =
        selectedPeriod != QuickPeriod.CUSTOM ||
                (customStart != null && customEnd != null)

    // ✅ STATS
    val stats by if (isPeriodValid) {
        viewModel.dailyStatsBetween(
            from = start,
            to = end,
            locationName = selectedLocation
        )
    } else {
        kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsState(initial = emptyList())


    val totalMinutes = stats.sumOf { it.totalMinutes }
    val overtimeMinutes =
        (totalMinutes - stats.size * dailyWorkMinutes).coerceAtLeast(0)

    // ✅ HEADER EXPORT (MAINTENANT OK)
    val exportHeader = remember(settings, start, end, selectedLocation) {
        ExportHeader(
            employeeName = settings.employeeName ?: "",
            employeeAddress = settings.employeeAddress ?: "",
            employerName = settings.employerName ?: "",
            employerAddress = settings.employerAddress ?: "",
            periodStart = start,
            periodEnd = end,
            city = settings.city ?: "Paris",
            location = selectedLocation ?: "Tous les lieux"
        )
    }


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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            onPeriodSelected = {
                                selectedPeriod = it
                                if (it != QuickPeriod.CUSTOM) {
                                    customStart = null
                                    customEnd = null
                                }
                            }
                        )

                        Spacer(Modifier.height(12.dp))

                        LocationFilterDropdown(
                            locations = locationNames,
                            selectedLocation = selectedLocation,
                            onLocationSelected = { selectedLocation = it }
                        )

                        if (selectedPeriod == QuickPeriod.CUSTOM) {
                            Spacer(Modifier.height(12.dp))
                            CustomDatePicker(
                                label = "Date de début",
                                date = customStart,
                                onDateSelected = { customStart = it }
                            )
                            Spacer(Modifier.height(8.dp))
                            CustomDatePicker(
                                label = "Date de fin",
                                date = customEnd,
                                onDateSelected = { customEnd = it + 86_399_999L }
                            )
                        }
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

            items(stats) { stat ->
                DaySummaryCard(stat)
            }

            /* ---------- EXPORT ---------- */
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = stats.isNotEmpty(),
                        onClick = {
                            scope.launch {
                                val file = CsvExportUtils.exportDailyStatsToCsv(
                                    context = context,
                                    stats = stats,
                                    periodStart = start,
                                    periodEnd = end
                                )
                                shareCsv(context, file)
                            }
                        }
                    ) {
                        Icon(Icons.Default.FileDownload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Exporter en CSV")
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = stats.isNotEmpty(),
                        onClick = {
                            scope.launch {
                                PdfExportUtils.exportStatisticsPdf(
                                    context = context,
                                    header = exportHeader,
                                    stats = stats,
                                    totalMinutes = totalMinutes
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Exporter en PDF")
                    }
                }
            }
        }

    }
}







/* ---------------- UI COMPONENTS ---------------- */

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DaySummaryCard(stat: DailyStat) {
    val sdf = remember { SimpleDateFormat("EEEE dd/MM", Locale.getDefault()) }

    Card {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                sdf.format(Date(stat.dayStart)),
                fontWeight = FontWeight.Medium
            )
            Text(
                "Lieu : ${stat.workLocationName}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(6.dp))
            Text(
                formatMinutes(stat.totalMinutes),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationFilterDropdown(
    locations: List<String>,
    selectedLocation: String?,
    onLocationSelected: (String?) -> Unit
)
{
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedLocation ?: "Tous les lieux",
            onValueChange = {},
            readOnly = true,
            label = { Text("Emplacement") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(expanded, { expanded = false }) {

            DropdownMenuItem(
                text = { Text("Tous les lieux") },
                onClick = {
                    expanded = false
                    onLocationSelected(null)
                }
            )

            locations.forEach { location ->
                DropdownMenuItem(
                    text = { Text(location) },
                    onClick = {
                        expanded = false
                        onLocationSelected(location)
                    }
                )
            }
        }
    }
}


/* ---------------- DATE PICKER ---------------- */

@Composable
fun CustomDatePicker(
    label: String,
    date: Long?,
    onDateSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    if (date != null) calendar.timeInMillis = date

    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        calendar.set(year, month, day, 0, 0, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        onDateSelected(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
    ) {
        OutlinedTextField(
            value = date?.let { formatter.format(Date(it)) } ?: "",
            onValueChange = {},
            enabled = false, // 🔑 IMPORTANT
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}


/* ---------------- HELPERS ---------------- */

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

/* ---------------- PERIODS ---------------- */

enum class QuickPeriod(val label: String) {
    THIS_WEEK("Cette semaine"),
    LAST_WEEK("Semaine précédente"),
    THIS_MONTH("Ce mois"),
    LAST_MONTH("Mois précédent"),
    CUSTOM("Personnalisé")
}

private fun computePeriod(period: QuickPeriod): Pair<Long, Long> {
    val c = Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return when (period) {

        QuickPeriod.THIS_WEEK -> {
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val start = c.timeInMillis
            val end = start + 7 * 86_400_000L - 1
            start to end
        }

        QuickPeriod.LAST_WEEK -> {
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            c.add(Calendar.WEEK_OF_YEAR, -1)
            val start = c.timeInMillis
            val end = start + 7 * 86_400_000L - 1
            start to end
        }

        QuickPeriod.THIS_MONTH -> {
            c.set(Calendar.DAY_OF_MONTH, 1)
            val start = c.timeInMillis
            c.add(Calendar.MONTH, 1)
            val end = c.timeInMillis - 1
            start to end
        }

        QuickPeriod.LAST_MONTH -> {
            c.set(Calendar.DAY_OF_MONTH, 1)
            c.add(Calendar.MONTH, -1)
            val start = c.timeInMillis
            c.add(Calendar.MONTH, 1)
            val end = c.timeInMillis - 1
            start to end
        }

        QuickPeriod.CUSTOM -> 0L to 0L
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickFilterDropdown(
    selectedPeriod: QuickPeriod,
    onPeriodSelected: (QuickPeriod) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedPeriod.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Période") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            QuickPeriod.values().forEach {
                DropdownMenuItem(
                    text = { Text(it.label) },
                    onClick = {
                        expanded = false
                        onPeriodSelected(it)
                    }
                )
            }
        }
    }



}
