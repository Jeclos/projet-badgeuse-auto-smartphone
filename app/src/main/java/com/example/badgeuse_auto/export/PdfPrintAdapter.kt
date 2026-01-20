package com.example.badgeuse_auto.export

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PdfPrintAdapter(
    private val context: Context,
    private val file: File
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        callback.onLayoutFinished(
            PrintDocumentInfo.Builder(file.name)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .build(),
            true
        )
    }

    override fun onWrite(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        FileInputStream(file).use { input ->
            FileOutputStream(destination.fileDescriptor).use { output ->
                input.copyTo(output)
            }
        }
        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
    }
}
