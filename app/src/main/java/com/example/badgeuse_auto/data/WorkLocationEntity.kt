package com.example.badgeuse_auto.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "work_locations")
data class WorkLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,
    val latitude: Double,
    val longitude: Double,

    // ✅ NOUVEAU : lieu actif ou non
    val isActive: Boolean = true,

    // ✅ ID STABLE POUR GEOFENCE
    val geofenceUid: String =
        "geo_${latitude.toString().take(8)}_${longitude.toString().take(8)}"

)
