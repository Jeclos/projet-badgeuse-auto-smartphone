package com.example.badgeuse_auto.utils

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.core.content.FileProvider
import java.io.File

fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(intent, "Partager le relevé PDF")
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
