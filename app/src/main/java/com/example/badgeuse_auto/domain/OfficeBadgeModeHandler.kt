package com.example.badgeuse_auto.domain

import com.example.badgeuse_auto.data.*

class OfficeBadgeModeHandler(
    private val presenceDao: PresenceDao
) : BadgeModeHandler {

    override suspend fun onEnter(
        now: Long,
        workLocation: WorkLocationEntity,
        currentPresence: PresenceEntity?
    ): String {

        if (currentPresence != null) {
            return "Déjà présent sur un lieu"
        }

        presenceDao.insert(
            PresenceEntity.autoEnter(
                workLocationId = workLocation.id,
                time = now,
                type = "AUTO_OFFICE"
            )
        )

        return "Arrivée automatique : ${workLocation.name}"
    }

    override suspend fun onExit(
        now: Long,
        workLocation: WorkLocationEntity,
        currentPresence: PresenceEntity?
    ): String {

        if (currentPresence == null) {
            return "Aucune présence en cours"
        }

        if (currentPresence.workLocationId != workLocation.id) {
            return "Départ ignoré (lieu différent)"
        }

        presenceDao.update(
            PresenceEntity.autoExit(
                current = currentPresence,
                time = now,
                type = "AUTO_OFFICE"
            )
        )

        return "Départ automatique : ${workLocation.name}"
    }
}
