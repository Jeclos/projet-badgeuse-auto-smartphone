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

    val enterType: String,
    val exitType: String? = null,
    val lastDepotExitTime: Long? = null,

    val locked: Boolean = false,

    val pendingEnterAt: Long? = null,
    val pendingExitAt: Long? = null,
    val isPending: Boolean = false,
    val isExitPending: Boolean = false,

    val timestamp: Long,
    val isEnter: Boolean,
    val isExit: Boolean,
    val minutesOfDay: Int
) {

    companion object {

        fun autoEnter(
            workLocationId: Long,
            time: Long,
            type: String
        ): PresenceEntity {

            val minutes = ((time / 60000) % 1440).toInt()

            return PresenceEntity(
                workLocationId = workLocationId,

                enterTime = time,
                enterType = type,

                timestamp = time,
                isEnter = true,
                isExit = false,
                minutesOfDay = minutes
            )
        }

        fun autoExit(
            current: PresenceEntity,
            time: Long,
            type: String
        ): PresenceEntity {

            val minutes = ((time / 60000) % 1440).toInt()

            return current.copy(
                exitTime = time,
                exitType = type,
                locked = true,

                timestamp = time,
                isEnter = false,
                isExit = true,
                minutesOfDay = minutes
            )
        }
    }
}
