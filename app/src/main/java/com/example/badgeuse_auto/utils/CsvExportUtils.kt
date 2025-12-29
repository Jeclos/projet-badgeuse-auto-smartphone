package com.example.badgeuse_auto.utils

import android.content.Context
import com.example.badgeuse_auto.data.DailyStat
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object CsvExportUtils {

    /**
     * Génère un CSV contenant :
     * date, work_location, total_minutes, total_hours
     *
     * @return File créé dans context.cacheDir
     */
    suspend fun exportDailyStatsToCsv(
        context: Context,
        stats: List<DailyStat>,
        periodStart: Long,
        periodEnd: Long
    ): File {

        val fileName = buildFileName(periodStart, periodEnd)
        val csvFile = File(context.cacheDir, fileName)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        FileWriter(csvFile).use { writer ->

            // Header CSV
            writer.append("date,work_location,total_minutes,total_hours\n")

            stats
                .sortedBy { it.dayStart }
                .forEach { stat ->

                    val date = sdf.format(Date(stat.dayStart))
                    val hours = stat.totalMinutes.toDouble() / 60.0

                    writer.append(date)
                        .append(',')
                        .append(escapeCsv(stat.workLocationName))
                        .append(',')
                        .append(stat.totalMinutes.toString())
                        .append(',')
                        .append(String.format(Locale.US, "%.2f", hours))
                        .append('\n')
                }

            writer.flush()
        }

        return csvFile
    }

    /**
     * Nom du fichier lisible :
     * releve_heures_YYYY-MM-DD_YYYY-MM-DD.csv
     */
    private fun buildFileName(start: Long, end: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startStr = sdf.format(Date(start))
        val endStr = sdf.format(Date(end))
        return "releve_heures_${startStr}_$endStr.csv"
    }

    /**
     * Sécurise les valeurs texte pour le CSV
     * (virgules, guillemets, etc.)
     */
    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
