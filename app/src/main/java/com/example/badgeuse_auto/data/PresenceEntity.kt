package com.example.badgeuse_auto.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presences")
data class PresenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val workLocationId: Long,

    val enterTime: Long,

    val exitTime: Long? = null,

    val enterType: String,      // MANUAL / AUTO
    val exitType: String? = null
)
