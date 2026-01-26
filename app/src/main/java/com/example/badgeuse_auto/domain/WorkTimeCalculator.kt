package com.example.badgeuse_auto.domain

import com.example.badgeuse_auto.data.*
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

object WorkTimeCalculator {

    fun computePayableMinutes(
        presencesOfDay: List<PresenceEntity>,
        settings: SettingsEntity
    ): Long {

        val rawMinutes = presencesOfDay.sumOf { presence ->
            val exit = presence.exitTime ?: return@sumOf 0L

            var start = presence.enterTime
            var end = exit

            if (settings.badgeMode == BadgeMode.DEPOT) {

                val dayStart = startOfWorkDay(
                    start,
                    settings.depotStartHour,
                    settings.depotStartMinute
                )

                val minStart = buildOfficialTime(
                    dayStart,
                    settings.depotStartHour,
                    settings.depotStartMinute
                )

                val maxEnd = buildOfficialTime(
                    dayStart,
                    settings.depotEndHour,
                    settings.depotEndMinute
                )

                start = max(start, minStart)
                end = min(end, maxEnd)
            }

            ((end - start) / 60_000).coerceAtLeast(0)
        }
        val lunchDeduction =
            if (settings.lunchBreakEnabled) {
                LunchBreakCalculator.computeLunchAbsenceMinutes(
                    presencesOfDay,
                    settings
                )
            } else {
                0L
            }
        return (rawMinutes - lunchDeduction + settings.depotDailyAdjustMin)
            .coerceAtLeast(0)

    }

    /* ---------------- UTILS ---------------- */

    private fun buildOfficialTime(
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

    fun startOfWorkDay(
        time: Long,
        startHour: Int,
        startMinute: Int
    ): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Si le badge est AVANT l'heure de début → jour précédent
        if (time < cal.timeInMillis) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }

        return cal.timeInMillis
    }
}
