package com.example.badgeuse_auto.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_location")
data class WorkLocationEntity(
    @PrimaryKey val id: Int = 1,   // Un seul lieu enregistré
    val name: String,
    val latitude: Double,
    val longitude: Double


)
