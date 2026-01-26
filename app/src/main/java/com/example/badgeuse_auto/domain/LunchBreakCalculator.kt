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

// ⛔ une seule présence : pause UNIQUEMENT si journée complète autour du déjeuner
        if (sorted.size < 2) {

            val window = computeLunchWindow(
                sorted.first().enterTime,
                settings
            )

            val presence = sorted.first()
            val presenceStart = presence.enterTime
            val presenceEnd = presence.exitTime ?: presence.enterTime

            // ✅ présent AVANT et APRÈS la plage déjeuner → pause
            if (presenceStart < window.start && presenceEnd > window.end) {
                return pauseMin
            }

            // ❌ demi-journée ou chevauchement partiel → pas de pause
            return 0L
        }



        val window = computeLunchWindow(
            sorted.first().enterTime,
            settings
        )

        var absenceInWindowMs = 0L
        var absenceOutsideWindowMs = 0L

        for (i in 0 until sorted.lastIndex) {
            val current = sorted[i]
            val next = sorted[i + 1]

            val gapStart = current.exitTime!!
            val gapEnd = next.enterTime

            // ⏱ total
            val gapMs = gapEnd - gapStart

            // 🔲 intersection avec la plage
            val overlapStart = max(gapStart, window.start)
            val overlapEnd = min(gapEnd, window.end)

            if (overlapEnd > overlapStart) {
                absenceInWindowMs += overlapEnd - overlapStart
            }

            // ❌ hors plage
            val outsideMs = gapMs -
                    max(0L, overlapEnd - overlapStart)

            if (outsideMs > 0) {
                absenceOutsideWindowMs += outsideMs
            }
        }

        val absenceInWindowMin = absenceInWindowMs / 60_000
        val absenceOutsideWindowMin = absenceOutsideWindowMs / 60_000

        // 🧠 LOGIQUE MÉTIER
        val effectiveLunchAbsence =
            max(pauseMin, absenceInWindowMin.toLong())

        return effectiveLunchAbsence + absenceOutsideWindowMin
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
