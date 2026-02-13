package com.example.badgeuse_auto.domain

import com.example.badgeuse_auto.data.PresenceEntity
import com.example.badgeuse_auto.data.SettingsEntity
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

object LunchBreakCalculator {

    fun computeLunchAbsenceMinutes(
        presences: List<PresenceEntity>,
        settings: SettingsEntity
    ): Long {

        if (!settings.lunchBreakEnabled) return 0L

        val pauseMin = settings.lunchDefaultDurationMin.toLong()

        val sorted = presences
            .filter { it.exitTime != null }
            .sortedBy { it.enterTime }

        if (sorted.isEmpty()) return 0L

        val window = computeLunchWindow(
            sorted.first().enterTime,
            settings
        )

        // 🧠 Détection journée complète (avant ET après le déjeuner)
        val dayStart = sorted.first().enterTime
        val dayEnd = sorted.last().exitTime!!

        val isFullDay =
            dayStart < window.start && dayEnd > window.end

        // ❌ Demi-journée → pas de pause déjeuner
        if (!isFullDay) return 0L

        // ⏱ Calcul de l’absence DANS la plage déjeuner
        var absenceInWindowMs = 0L

        for (i in 0 until sorted.lastIndex) {
            val current = sorted[i]
            val next = sorted[i + 1]

            val gapStart = current.exitTime!!
            val gapEnd = next.enterTime

            val overlapStart = max(gapStart, window.start)
            val overlapEnd = min(gapEnd, window.end)

            if (overlapEnd > overlapStart) {
                absenceInWindowMs += overlapEnd - overlapStart
            }
        }

        val absenceInWindowMin = absenceInWindowMs / 60_000

        // 🧠 LOGIQUE QUOTA (FIN DU DOUBLE RETRAIT)
        return max(pauseMin - absenceInWindowMin, 0L)
    }

    private fun computeLunchWindow(
        referenceTime: Long,
        settings: SettingsEntity
    ): TimeWindow {

        val cal = Calendar.getInstance().apply {
            timeInMillis = referenceTime
            set(Calendar.HOUR_OF_DAY, settings.lunchWindowStartHour)
            set(Calendar.MINUTE, settings.lunchWindowStartMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, settings.lunchWindowEndHour)
        cal.set(Calendar.MINUTE, settings.lunchWindowEndMinute)

        val end = cal.timeInMillis

        return TimeWindow(start, end)
    }

    data class TimeWindow(
        val start: Long,
        val end: Long
    )
}
