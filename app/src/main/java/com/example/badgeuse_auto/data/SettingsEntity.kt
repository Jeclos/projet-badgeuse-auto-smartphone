package com.example.badgeuse_auto.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,

    val employeeName: String = "",
    val employeeAddress: String = "",

    val employerName: String = "",
    val employerAddress: String = "",
    val city: String = "",

    val enterDistance: Int = 150,
    val exitDistance: Int = 150,

    val enterDelaySec: Int = 0,
    val exitDelaySec: Int = 0,

    val dailyWorkHours: Int = 7,

    val lunchBreakEnabled: Boolean = false,
    val lunchBreakOutside: Boolean = true,
    val lunchBreakDurationMin: Int = 60,

    /* 🔥 NOUVEAU MODE */
    val badgeMode: BadgeMode = BadgeMode.OFFICE,

    // ✅ HOME / TRAVEL (AJOUT)
    val homeLatitude: Double = 0.0,
    val homeLongitude: Double = 0.0,
    val travelTimeMin: Int = 0,


    /* 🔥 MODE DEPOT */
    val depotStartHour: Int = 7,
    val depotStartMinute: Int = 0,
    val depotEndHour: Int = 17,
    val depotEndMinute: Int = 0,

    /** Peut être négatif ou positif */
    val depotDailyAdjustMin: Int = 0,

    /* 🎨 UI */
    val appStyle: String = "PRO",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,

    // 🔑 PENDING ENTER
    val pendingEnterUid: String? = null,
    val pendingEnterToken: Long? = null,

    // Pause déjeuner
    val lunchEnabled: Boolean = false,

// plage autorisée
    val lunchWindowStartHour: Int = 12,
    val lunchWindowStartMinute: Int = 0,
    val lunchWindowEndHour: Int = 14,
    val lunchWindowEndMinute: Int = 0,

// durée minimale déclenchante
    val lunchMinDurationMin: Int = 45,

// durée standard à déduire si pause reconnue
    val lunchDefaultDurationMin: Int = 45

)


enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
