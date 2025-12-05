package com.example.badgeuse_auto.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,

    val enterDistance: Int = 150,   // distance entrée en mètres
    val exitDistance: Int = 150,    // distance sortie en mètres

    val enterDelaySec: Int = 0,     // délai entrée (sec)
    val exitDelaySec: Int = 0,       // délai sortie  (sec)
    val dailyWorkHours: Int = 7
    )
