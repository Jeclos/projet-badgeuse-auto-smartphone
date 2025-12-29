package com.example.badgeuse_auto.ui.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
fun formatClock(timeMillis: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timeMillis))

fun formatMinutes(minutes: Long): String =
    "${minutes / 60}h ${minutes % 60}min"

fun formatDate(timeMillis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timeMillis))
