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
            PresenceEntity(
                workLocationId = workLocation.id,
                enterTime = now,
                enterType = "AUTO"
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
            currentPresence.copy(
                exitTime = now,
                exitType = "AUTO"
            )
        )

        return "Départ automatique : ${workLocation.name}"
    }
}
