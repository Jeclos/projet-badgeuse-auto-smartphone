package com.example.badgeuse_auto.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewScreen(
    pdfPath: String,
    onShare: (File) -> Unit,
    onPrint: (File) -> Unit
) {
    val context = LocalContext.current
    val file = remember(pdfPath) { File(pdfPath) }

    val bitmaps by remember {
        mutableStateOf(renderPdfPages(context, file))
    }

    /** 🔍 Zoom & déplacement */
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val minScale = 1f
    val maxScale = 5f

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Aperçu PDF") })
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
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
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(minScale, maxScale)

                        // Pan uniquement si zoom > 1
                        if (newScale > 1f) {
                            offset += pan
                        } else {
                            offset = Offset.Zero
                        }

                        scale = newScale
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                bitmaps.forEach { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/* =========================================================
   RENDER PDF → BITMAPS
   ========================================================= */

fun renderPdfPages(context: Context, file: File): List<Bitmap> {
    val fileDescriptor =
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

    val renderer = PdfRenderer(fileDescriptor)
    val bitmaps = mutableListOf<Bitmap>()

    for (i in 0 until renderer.pageCount) {
        val page = renderer.openPage(i)

        val scale = 2.5f // Qualité visuelle (2.0 à 3.0)

        val bitmap = Bitmap.createBitmap(
            (page.width * scale).toInt(),
            (page.height * scale).toInt(),
            Bitmap.Config.ARGB_8888
        )

        val matrix = android.graphics.Matrix().apply {
            setScale(scale, scale)
        }

        page.render(
            bitmap,
            null,
            matrix,
            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
        )

        bitmaps.add(bitmap)
        page.close()
    }

    renderer.close()
    fileDescriptor.close()

    return bitmaps
}
