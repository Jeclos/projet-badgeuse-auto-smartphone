package com.example.badgeuse_auto.utils

import android.content.Context
import com.example.badgeuse_auto.data.DailyWorkSummary
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object CsvExportUtils {

    /**
     * Genère un CSV contenant : date,total_minutes,total_hours
     * @return File créé dans context.cacheDir
     */
    suspend fun exportSummariesToCsv(
        context: Context,
        summaries: List<DailyWorkSummary>,
        fileName: String = "work_summary_${System.currentTimeMillis()}.csv"
    ): File {
        val csvFile = File(context.cacheDir, fileName)
        FileWriter(csvFile).use { writer ->
            // Header
            writer.append("date,total_minutes,total_hours\n")
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            for (s in summaries.sortedBy { it.dayStart }) {
                val date = sdf.format(Date(s.dayStart))
                val hours = s.totalMinutes.toDouble() / 60.0
                // write line: 2025-02-01,480,8.0
                writer.append(date)
                    .append(',')
                    .append(s.totalMinutes.toString())
                    .append(',')
                    .append(String.format(Locale.US, "%.2f", hours))
                    .append('\n')
            }
            writer.flush()
        }
        return csvFile
    }
}
