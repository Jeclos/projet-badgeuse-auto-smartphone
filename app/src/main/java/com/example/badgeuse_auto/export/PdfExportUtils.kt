package com.example.badgeuse_auto.export

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.badgeuse_auto.data.DailyStat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExportUtils {

    fun exportStatisticsPdf(
        context: Context,
        header: ExportHeader,
        stats: List<DailyStat>,
        totalMinutes: Long
    ) {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val textPaint = Paint().apply {
            textSize = 11f
            color = android.graphics.Color.DKGRAY
        }

        val boldPaint = Paint(textPaint).apply {
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }

        val titlePaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            color = android.graphics.Color.BLACK
        }

        val tableHeaderPaint = Paint(textPaint).apply {
            isFakeBoldText = true
            color = android.graphics.Color.WHITE
        }

        val tableBgPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1976D2")
        }

        val linePaint = Paint().apply {
            color = android.graphics.Color.LTGRAY
            strokeWidth = 1f
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var y = 40

        // ---------- TITRE ----------
        canvas.drawText("RELEVÉ D’HEURES", 297f, y.toFloat(), titlePaint)
        y += 40

        // ---------- EMPLOYÉ (GAUCHE) ----------
        canvas.drawText(header.employeeName, 40f, y.toFloat(), boldPaint)
        y += 14
        splitAddress(header.employeeAddress).forEach {
            canvas.drawText(it, 40f, y.toFloat(), textPaint)
            y += 14
        }

        // ---------- EMPLOYEUR (DROITE) ----------
        var rightY = 80
        canvas.drawText("À l’attention de :", 350f, rightY.toFloat(), boldPaint)
        rightY += 14
        canvas.drawText(header.employerName, 350f, rightY.toFloat(), textPaint)
        rightY += 14
        splitAddress(header.employerAddress).forEach {
            canvas.drawText(it, 350f, rightY.toFloat(), textPaint)
            rightY += 14
        }

        y = maxOf(y + 20, rightY + 20)

        // ---------- FAIT LE ----------
        canvas.drawText(
            "Fait le ${sdf.format(Date())}",
            40f,
            y.toFloat(),
            textPaint
        )
        y += 30

        // ---------- PERIODE ----------
        canvas.drawText(
            "Période du ${sdf.format(Date(header.periodStart))} au ${
                sdf.format(Date(header.periodEnd))
            }",
            40f,
            y.toFloat(),
            boldPaint
        )
        y += 30

        // ---------- TABLE HEADER ----------
        canvas.drawRect(40f, y.toFloat(), 555f, (y + 24).toFloat(), tableBgPaint)
        canvas.drawText("Date", 50f, (y + 17).toFloat(), tableHeaderPaint)
        canvas.drawText("Lieu", 170f, (y + 17).toFloat(), tableHeaderPaint)
        canvas.drawText("Durée", 480f, (y + 17).toFloat(), tableHeaderPaint)
        y += 34

        // ---------- TABLE ROWS ----------
        stats.forEach { stat ->
            canvas.drawText(
                sdf.format(Date(stat.dayStart)),
                50f,
                y.toFloat(),
                textPaint
            )
            canvas.drawText(stat.workLocationName, 170f, y.toFloat(), textPaint)

            val h = stat.totalMinutes / 60
            val m = stat.totalMinutes % 60
            canvas.drawText(
                "${h}h${m.toString().padStart(2, '0')}",
                480f,
                y.toFloat(),
                textPaint
            )

            y += 18
            canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), linePaint)
            y += 8
        }

        // ---------- TOTAL ----------
        val totalH = totalMinutes / 60
        val totalM = totalMinutes % 60
        y += 20
        canvas.drawText(
            "TOTAL PÉRIODE : ${totalH}h${totalM.toString().padStart(2, '0')}",
            40f,
            y.toFloat(),
            boldPaint
        )

        pdf.finishPage(page)

        val fileName = ExportFileNameUtils.buildFileName(
            baseName = "releve_heures",
            start = header.periodStart,
            end = header.periodEnd,
            extension = "pdf"
        )

        val file = File(context.cacheDir, fileName)
        pdf.writeTo(FileOutputStream(file))
        pdf.close()

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Partager le relevé PDF"
            )
        )
    }

    private fun splitAddress(address: String): List<String> {
        val regex = Regex("(.*?)(\\d{5}.*)")
        val match = regex.find(address.trim())

        return if (match != null) {
            listOf(
                match.groupValues[1].trim(),
                match.groupValues[2].trim()
            )
        } else {
            address.lines()
        }
    }
}
