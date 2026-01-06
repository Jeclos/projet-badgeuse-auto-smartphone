package com.example.badgeuse_auto.domain

import com.example.badgeuse_auto.data.*
import java.util.*

class DepotBadgeModeHandler(
    private val presenceDao: PresenceDao,
    private val settings: SettingsEntity
) : BadgeModeHandler {

    override suspend fun onEnter(
        now: Long,
        workLocation: WorkLocationEntity,
        currentPresence: PresenceEntity?
    ): String {

        if (currentPresence != null) {
            return "Passage dépôt ignoré"
        }

        val officialStart = buildTime(
            now,
            settings.depotStartHour,
            settings.depotStartMinute
        )

        presenceDao.insert(
            PresenceEntity(
                workLocationId = workLocation.id,
                enterTime = maxOf(now, officialStart),
                enterType = "AUTO_DEPOT"
            )
        )

        return "Début de journée dépôt"
    }

    override suspend fun onExit(
        now: Long,
        workLocation: WorkLocationEntity,
        currentPresence: PresenceEntity?
    ): String {
        return "Sortie dépôt gérée par règle centrale"
    }


    private fun buildTime(reference: Long, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = reference
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}
