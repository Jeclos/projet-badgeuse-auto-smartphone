package com.example.badgeuse_auto.data

data class DailyStat(
    val dayStart: Long,
    val totalMinutes: Long,
    val workLocationName: String,
    val detail: String = ""
)
