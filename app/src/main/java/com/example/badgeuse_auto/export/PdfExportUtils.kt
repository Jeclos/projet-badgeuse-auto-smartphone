package com.example.badgeuse_auto.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.badgeuse_auto.data.DailyStat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExportUtils {

    fun generateStatisticsPdf(
        context: Context,
        header: ExportHeader,
        stats: List<DailyStat>,
        totalMinutes: Long
    ): File {

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        // ---------- PAINTS ----------
        val textPaint = Paint().apply {
            textSize = 9f
            color = android.graphics.Color.DKGRAY
            isAntiAlias = true
        }

        val detailTextPaint = TextPaint().apply {
            textSize = 9f
            color = android.graphics.Color.DKGRAY
            isAntiAlias = true
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

        // ---------- LAYOUT ----------
        val pageWidth = pageInfo.pageWidth.toFloat()
        val leftMargin = 40f
        val rightMargin = 40f

        val xDate = 40f
        val xDetails = 170f
        val xDuration = pageWidth - 75f

        val baselineOffset = textPaint.textSize + 4
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        var y = 40f

        // ---------- TITRE ----------
        canvas.drawText("RELEVÉ D’HEURES", pageWidth / 2, y.toFloat(), titlePaint)
        y += 40

        // ---------- EMPLOYÉ ----------
        canvas.drawText(header.employeeName, leftMargin, y.toFloat(), boldPaint)
        y += 14
        splitAddress(header.employeeAddress).forEach {
            canvas.drawText(it, leftMargin, y.toFloat(), textPaint)
            y += 14
        }

        // ---------- EMPLOYEUR ----------
        var rightY = 80
        canvas.drawText("À l’attention de :", 350f, rightY.toFloat(), boldPaint)
        rightY += 14
        canvas.drawText(header.employerName, 350f, rightY.toFloat(), textPaint)
        rightY += 14
        splitAddress(header.employerAddress).forEach {
            canvas.drawText(it, 350f, rightY.toFloat(), textPaint)
            rightY += 14
        }

        y = maxOf(y + 20f, rightY.toFloat() + 20f)


        // ---------- DATE ----------
        canvas.drawText(
            "Fait le ${sdf.format(Date())}",
            leftMargin,
            y.toFloat(),
            textPaint
        )
        y += 30

        // ---------- PERIODE ----------
        canvas.drawText(
            "Période du ${sdf.format(Date(header.periodStart))} au ${
                sdf.format(Date(header.periodEnd))
            }",
            leftMargin,
            y.toFloat(),
            boldPaint
        )
        y += 30

        // ---------- TABLE HEADER ----------
        canvas.drawRect(leftMargin, y.toFloat(), pageWidth - rightMargin, (y + 24).toFloat(), tableBgPaint)
        canvas.drawText("Date", xDate + 10f, y + 17f, tableHeaderPaint)
        canvas.drawText("Pointages", xDetails, y + 17f, tableHeaderPaint)
        canvas.drawText("Durée", xDuration, y + 17f, tableHeaderPaint)
        y += 34

        // ---------- TABLE ROWS ----------
        stats.forEach { stat ->

            // --- DATE ---
            val dateStr = sdf.format(Date(stat.dayStart))
            canvas.drawText(dateStr, xDate, y + baselineOffset, textPaint)

            // --- DETAILS ---
            val detailsHeight = drawMultilineText(
                canvas = canvas,
                text = stat.detail.ifBlank { "-" },
                x = xDetails,
                y = y,
                width = (xDuration - xDetails - 10).toInt(),
                paint = detailTextPaint
            )

            // --- DURÉE ---
            val h = stat.totalMinutes / 60
            val m = stat.totalMinutes % 60
            canvas.drawText(
                "${h}h${m.toString().padStart(2, '0')}",
                xDuration,
                y + baselineOffset,
                textPaint
            )

            // --- HAUTEUR DYNAMIQUE ---
            val rowHeight = maxOf(18f, detailsHeight.toFloat())
            y += rowHeight + 6f

            // --- SEPARATEUR ---
            canvas.drawLine(leftMargin, y, pageWidth - rightMargin, y, linePaint)
            y += 6f
        }

        // ---------- TOTAL ----------
        val totalH = totalMinutes / 60
        val totalM = totalMinutes % 60
        y += 20
        canvas.drawText(
            "TOTAL PÉRIODE : ${totalH}h${totalM.toString().padStart(2, '0')}",
            leftMargin,
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

        return file
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

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Int,
        paint: TextPaint
    ): Int {

        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()

        return layout.height
    }
}
