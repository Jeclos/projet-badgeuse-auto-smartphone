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
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdf.startPage(pageInfo)
        var canvas = page.canvas

        // FOND BLANC EXPLICITE (ANTI DARK MODE)
        val backgroundPaint = Paint().apply {
            color = android.graphics.Color.WHITE
        }
        canvas.drawRect(
            0f,
            0f,
            pageInfo.pageWidth.toFloat(),
            pageInfo.pageHeight.toFloat(),
            backgroundPaint
        )

        // ---------- PAINTS ----------
        val textPaint = Paint().apply {
            textSize = 9f
            color = android.graphics.Color.DKGRAY
            isAntiAlias = true
        }
        val boldPaint = Paint(textPaint).apply {
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }

        val rightBoldPaint = Paint(boldPaint).apply {
            textAlign = Paint.Align.RIGHT
        }

        val rightTextPaint = Paint(textPaint).apply {
            textAlign = Paint.Align.RIGHT
        }

        val detailTextPaint = TextPaint().apply {
            textSize = 9f
            color = android.graphics.Color.DKGRAY
            isAntiAlias = true
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
        val leftMargin = 24f
        val rightMargin = 24f
        val maxY = pageInfo.pageHeight - 60f
        val xDate = leftMargin
        val xDetails = leftMargin + 120f
        val xDuration = pageWidth - rightMargin - 40f
        val xTotalLabel = xDuration - 120f
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
        canvas.drawRect(
            leftMargin,
            y.toFloat(),
            pageWidth - rightMargin,
            (y + 24).toFloat(),
            tableBgPaint
        )
        canvas.drawText("Date", xDate + 10f, y + 17f, tableHeaderPaint)
        canvas.drawText("Pointages", xDetails, y + 17f, tableHeaderPaint)
        canvas.drawText("Durée", xDuration, y + 17f, tableHeaderPaint)
        y += 34
        // ---------- PAGINATION ----------
        fun checkPageOverflow(requiredHeight: Float) {
            if (y + requiredHeight > maxY) {

                // terminer la page courante
                pdf.finishPage(page)

                // nouvelle page
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdf.startPage(pageInfo)
                canvas = page.canvas

                // fond blanc
                canvas.drawRect(
                    0f,
                    0f,
                    pageInfo.pageWidth.toFloat(),
                    pageInfo.pageHeight.toFloat(),
                    backgroundPaint
                )

                // reset Y
                y = 40f

                // redessiner l'en-tête du tableau
                drawTableHeader(
                    canvas,
                    y,
                    pageWidth,
                    leftMargin,
                    rightMargin,
                    xDate,
                    xDetails,
                    xDuration,
                    tableBgPaint,
                    tableHeaderPaint
                )

                y += 34
            }
        }


        // ---------- TABLE ROWS ----------
        var currentWeek: Pair<Int, Int>? = null
        var weeklyTotalMinutes = 0L

        val sortedStats = stats.sortedBy { it.dayStart }

        sortedStats.forEach { stat ->

            val statWeek = weekKey(stat.dayStart)

            if (currentWeek != null && statWeek != currentWeek) {
                val h = weeklyTotalMinutes / 60
                val m = weeklyTotalMinutes % 60

                checkPageOverflow(30f)

                y = drawInlineTotal(
                    canvas = canvas,
                    label = "TOTAL SEMAINE",
                    value = "${h}h${m.toString().padStart(2, '0')}",
                    x = xDetails,
                    yStart = y + 6f,
                    paint = boldPaint
                )

                y += 6f


                weeklyTotalMinutes = 0
            }

            currentWeek = statWeek
            weeklyTotalMinutes += stat.totalMinutes

            val dateStr = sdf.format(Date(stat.dayStart))
            checkPageOverflow(50f)
            canvas.drawText(dateStr, xDate, y + baselineOffset, textPaint)

            val detailsHeight = drawMultilineText(
                canvas,
                stat.detail.ifBlank { "-" },
                xDetails,
                y,
                (xDuration - xDetails - 10).toInt(),
                detailTextPaint
            )

            val h = stat.totalMinutes / 60
            val m = stat.totalMinutes % 60
            canvas.drawText(
                "${h}h${m.toString().padStart(2, '0')}",
                xDuration,
                y + baselineOffset,
                textPaint
            )

            val rowHeight = maxOf(18f, detailsHeight.toFloat())
            y += rowHeight + 4f
            canvas.drawLine(leftMargin, y, pageWidth - rightMargin, y, linePaint)
            y += 4f

        }

// ---------- DERNIÈRE SEMAINE ----------
        if (weeklyTotalMinutes > 0) {
            checkPageOverflow(30f)

            val h = weeklyTotalMinutes / 60
            val m = weeklyTotalMinutes % 60

            y = drawInlineTotal(
                canvas = canvas,
                label = "TOTAL SEMAINE",
                value = "${h}h${m.toString().padStart(2, '0')}",
                x = xDetails,
                yStart = y + 6f,
                paint = boldPaint
            )

            y += 6f

        }


// ---------- TOTAL PÉRIODE ----------
        val totalH = totalMinutes / 60
        val totalM = totalMinutes % 60
            checkPageOverflow(40f)

            y = drawInlineTotal(
                canvas = canvas,
                label = "TOTAL PÉRIODE",
                value = "${totalH}h${totalM.toString().padStart(2, '0')}",
                x = xDetails,
                yStart = y + 10f,
                paint = boldPaint
            )



// ---------- FINALISATION PDF ----------
        pdf.finishPage(page)

        val fileName = ExportFileNameUtils.buildFileName(
            "releve_heures",
            header.periodStart,
            header.periodEnd,
            "pdf"
        )

        val exportDir = File(context.filesDir, "exports").apply {
            if (!exists()) mkdirs()
        }
        val file = File(exportDir, fileName)
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
    private fun drawTableHeader(
        canvas: Canvas,
        y: Float,
        pageWidth: Float,
        leftMargin: Float,
        rightMargin: Float,
        xDate: Float,
        xDetails: Float,
        xDuration: Float,
        bgPaint: Paint,
        textPaint: Paint
    ) {
        canvas.drawRect(
            leftMargin,
            y,
            pageWidth - rightMargin,
            y + 24,
            bgPaint
        )
        canvas.drawText("Date", xDate + 10f, y + 17f, textPaint)
        canvas.drawText("Pointages", xDetails, y + 17f, textPaint)
        canvas.drawText("Durée", xDuration, y + 17f, textPaint)
    }

}
private fun weekKey(timestamp: Long): Pair<Int, Int> {
    val cal = Calendar.getInstance().apply {
        timeInMillis = timestamp
        firstDayOfWeek = Calendar.MONDAY
        minimalDaysInFirstWeek = 4
    }
    return cal.get(Calendar.YEAR) to cal.get(Calendar.WEEK_OF_YEAR)
}
private fun drawInlineTotal(
    canvas: Canvas,
    label: String,
    value: String,
    x: Float,
    yStart: Float,
    paint: Paint
): Float {

    val lineHeight = paint.textSize + 12f
    val text = "$label : $value"

    canvas.drawText(
        text,
        x,
        yStart + paint.textSize,
        paint
    )

    return yStart + lineHeight
}
