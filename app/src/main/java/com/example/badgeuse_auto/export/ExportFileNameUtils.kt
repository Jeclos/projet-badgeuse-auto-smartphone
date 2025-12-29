package com.example.badgeuse_auto.export

import java.text.SimpleDateFormat
import java.util.*

object ExportFileNameUtils {

    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun buildFileName(
        baseName: String,
        start: Long,
        end: Long,
        extension: String
    ): String {
        val startStr = fileDateFormat.format(Date(start))
        val endStr = fileDateFormat.format(Date(end))

        return "${baseName}_${startStr}_${endStr}.${extension}"
    }
}
