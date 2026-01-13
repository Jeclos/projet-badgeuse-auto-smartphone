package com.example.badgeuse_auto.ui.preview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun PdfPreviewScreen(
    pdfPath: String,
    onShare: (File) -> Unit,
    onPrint: (File) -> Unit
) {
    val context = LocalContext.current
    val file = remember { File(pdfPath) }

    val bitmap = remember {
        renderPdf(context, file)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { onShare(file) }) {
                Text("Exporter")
            }
            Button(onClick = { onPrint(file) }) {
                Text("Imprimer")
            }
        }
    }
}

fun renderPdf(context: Context, file: File): Bitmap {
    val fileDescriptor =
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

    val renderer = PdfRenderer(fileDescriptor)
    val page = renderer.openPage(0)

    val bitmap = Bitmap.createBitmap(
        page.width,
        page.height,
        Bitmap.Config.ARGB_8888
    )

    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

    page.close()
    renderer.close()
    fileDescriptor.close()

    return bitmap
}
