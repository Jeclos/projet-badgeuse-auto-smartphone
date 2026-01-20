package com.example.badgeuse_auto.export

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

fun sharePdf(context: Context, file: File) {
    Log.e("PDF_SHARE", "sharePdf CALLED")
    Log.e("PDF_SHARE", "File exists = ${file.exists()}")
    Log.e("PDF_SHARE", "Path = ${file.absolutePath}")

    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )

    Log.e("PDF_SHARE", "URI = $uri")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)

        clipData = ClipData.newUri(
            context.contentResolver,
            "PDF",
            uri
        )

        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // ⭐⭐ CRITIQUE ⭐⭐
    }

    context.startActivity(
        Intent.createChooser(intent, "Partager le relevé PDF")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun printPdf(context: Context, file: File) {
    val printManager =
        context.getSystemService(Context.PRINT_SERVICE) as PrintManager

    printManager.print(
        "Relevé d'heures",
        PdfPrintAdapter(context, file),
        PrintAttributes.Builder().build()
    )
}
