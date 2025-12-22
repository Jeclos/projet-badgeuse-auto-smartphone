package com.example.badgeuse_auto.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,

    val enterDistance: Int = 150,
    val exitDistance: Int = 150,

    val enterDelaySec: Int = 0,
    val exitDelaySec: Int = 0,

    val dailyWorkHours: Int = 7,

    // 🔹 PAUSE DÉJEUNER
    val lunchBreakEnabled: Boolean = false,
    val lunchBreakOutside: Boolean = true,
    val lunchBreakDurationMin: Int = 60
)

