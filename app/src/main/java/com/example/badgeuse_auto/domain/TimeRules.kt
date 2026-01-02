package com.example.badgeuse_auto.domain

import com.example.badgeuse_auto.data.SettingsEntity

object TimeRules {

    fun applyLunchBreak(
        workedMinutes: Long,
        settings: SettingsEntity
    ): Long {
        if (!settings.lunchBreakEnabled) return workedMinutes
        if (workedMinutes < 360) return workedMinutes

        return workedMinutes - settings.lunchBreakDurationMin
    }
}
