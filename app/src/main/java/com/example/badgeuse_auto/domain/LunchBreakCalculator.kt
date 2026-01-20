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

        if (!settings.lunchEnabled) return 0L

        val sorted = presences
            .filter { it.exitTime != null }
            .sortedBy { it.enterTime }

        if (sorted.size < 2) return 0L

        val window = computeLunchWindow(
            sorted.first().enterTime,
            settings
        )

        var totalAbsenceMs = 0L

        for (i in 0 until sorted.lastIndex) {
            val current = sorted[i]
            val next = sorted[i + 1]

            val gapStart = current.exitTime!!
            val gapEnd = next.enterTime

            val overlapStart = max(gapStart, window.start)
            val overlapEnd = min(gapEnd, window.end)

            if (overlapEnd > overlapStart) {
                totalAbsenceMs += overlapEnd - overlapStart
            }
        }

        val absenceMinutes = totalAbsenceMs / 60_000

        return if (absenceMinutes >= settings.lunchMinDurationMin)
            settings.lunchDefaultDurationMin.toLong()
        else
            0L
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
