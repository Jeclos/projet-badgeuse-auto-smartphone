package com.example.badgeuse_auto.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_work_summary")
data class DailyWorkSummary(
    @PrimaryKey val dayStart: Long, // epoch millis at 00:00:00 for the day
    val totalMinutes: Int
)
