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
                    time = start,
                    startHour = settings.depotStartHour,
                    startMinute = settings.depotStartMinute,
                    endHour = settings.depotEndHour,
                    endMinute = settings.depotEndMinute
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
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ): Long {

        val cal = Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 🧠 Horaire de nuit si le début est APRÈS la fin
        val isNightShift =
            startHour > endHour ||
                    (startHour == endHour && startMinute > endMinute)

        // ⛔ retour veille UNIQUEMENT pour les nuits
        if (isNightShift && time < cal.timeInMillis) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }

        return cal.timeInMillis
    }
}
