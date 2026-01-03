package com.example.badgeuse_auto.domain

import com.example.badgeuse_auto.data.*
import com.example.badgeuse_auto.data.SettingsEntity

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
                enterType = "AUTO_HOME_TRAVEL"
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
            .coerceAtLeast(current.enterTime) // 🛡️ jamais négatif ni incohérent

        presenceDao.update(
            current.copy(
                exitTime = endTime,
                exitType = "AUTO_HOME_TRAVEL",
                locked = true
            )
        )

        return "Retour domicile → fin à $endTime"
    }

}
