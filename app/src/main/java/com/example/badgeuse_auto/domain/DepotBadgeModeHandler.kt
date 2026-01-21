package com.example.badgeuse_auto.domain

import com.example.badgeuse_auto.data.PresenceDao
import com.example.badgeuse_auto.data.PresenceEntity
import com.example.badgeuse_auto.data.SettingsEntity
import com.example.badgeuse_auto.data.WorkLocationEntity
import java.util.Calendar

class DepotBadgeModeHandler(
    private val presenceDao: PresenceDao,
    private val settings: SettingsEntity
) : BadgeModeHandler {

    override suspend fun onEnter(
        now: Long,
        workLocation: WorkLocationEntity,
        currentPresence: PresenceEntity?
    ): String {

        // Déjà présent → on ignore
        if (currentPresence != null) {
            return "Passage dépôt ignoré (déjà présent)"
        }

        // Heure officielle de début dépôt
        val officialStart = buildTime(
            reference = now,
            hour = settings.depotStartHour,
            minute = settings.depotStartMinute
        )

        presenceDao.insert(
            PresenceEntity(
                timestamp = now,
                minutesOfDay = ((now / 60000L) % 1440).toInt(),

                isEnter = true,
                isExit = false,

                workLocationId = workLocation.id,

                enterTime = maxOf(now, officialStart),
                exitTime = null,

                enterType = "AUTO_DEPOT",
                exitType = null
            )
        )

        return "Début de journée dépôt"
    }

    override suspend fun onExit(
        now: Long,
        workLocation: WorkLocationEntity,
        currentPresence: PresenceEntity?
    ): String {
        // La sortie dépôt est gérée ailleurs (règle centrale)
        return "Sortie dépôt gérée par règle centrale"
    }

    private fun buildTime(
        reference: Long,
        hour: Int,
        minute: Int
    ): Long =
        Calendar.getInstance().apply {
            timeInMillis = reference
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}
