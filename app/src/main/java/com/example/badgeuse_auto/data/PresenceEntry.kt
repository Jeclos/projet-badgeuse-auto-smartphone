package com.example.badgeuse_auto.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presence_table")
data class PresenceEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val timestamp: Long,
    val locationName: String,
    val source: String
)
