package com.example.badgeuse_auto.domain

import com.example.badgeuse_auto.data.*

class HomeTravelBadgeModeHandler(
    private val presenceDao: PresenceDao,
    private val settings: SettingsEntity
) : BadgeModeHandler {

    private val travelOffsetMs =
        settings.travelTimeMin * 60_000L

    override suspend fun onExit(
        now: Long,
        location: WorkLocationEntity,
        current: PresenceEntity?
    ): String {

        if (current != null) {
            return "Journée déjà démarrée"
        }

        val startTime = (now - travelOffsetMs)
            .coerceAtLeast(0L) // 🛡️ sécurité absolue

        presenceDao.insert(
            PresenceEntity(
                workLocationId = location.id,

                enterTime = startTime,
                exitTime = null,

                enterType = "AUTO_HOME_TRAVEL",
                exitType = null,
                lastDepotExitTime = null,

                locked = false,

                pendingEnterAt = null,
                pendingExitAt = null,
                isPending = false,
                isExitPending = false,

                timestamp = now,
                minutesOfDay = ((startTime / 60000) % 1440).toInt(),

                isEnter = true,
                isExit = false
            )
        )

        return "Départ domicile → journée à $startTime"
    }

    override suspend fun onEnter(
        now: Long,
        location: WorkLocationEntity,
        current: PresenceEntity?
    ): String {

        if (current == null || current.exitTime != null) {
            return "Aucune journée à clôturer"
        }

        val endTime = (now - travelOffsetMs)
            .coerceAtLeast(current.enterTime) // 🛡️ jamais incohérent

        presenceDao.update(
            current.copy(
                exitTime = endTime,
                exitType = "AUTO_HOME_TRAVEL",
                locked = true,
                timestamp = now,
                isExit = true
            )
        )

        return "Retour domicile → fin à $endTime"
    }
}
