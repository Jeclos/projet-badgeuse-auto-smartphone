package com.example.badgeuse_auto.data

import kotlinx.coroutines.flow.Flow

class SettingsRepository(
    private val settingsDao: SettingsDao
) {

    // --------------------------------------------------
    // CHARGER LES PARAMÈTRES (toujours une valeur)
    // --------------------------------------------------
    suspend fun getSettings(): SettingsEntity {
        return settingsDao.getSettings() ?: SettingsEntity()
    }

    // --------------------------------------------------
    // SAUVEGARDE COMPLÈTE DES PARAMÈTRES
    // --------------------------------------------------
    suspend fun saveSettings(settings: SettingsEntity) {
        settingsDao.insert(settings)
    }

    // --------------------------------------------------
    // OBSERVER LES PARAMÈTRES
    // --------------------------------------------------
    fun getSettingsFlow(): Flow<SettingsEntity?> {
        return settingsDao.getSettingsFlow()
    }

    // --------------------------------------------------
    // MAJ HEURES JOURNALIÈRES (EXISTANT, CONSERVÉ)
    // --------------------------------------------------
    suspend fun updateDailyWorkHours(hours: Int) {
        val current = settingsDao.getSettings() ?: SettingsEntity()
        settingsDao.insert(
            current.copy(
                dailyWorkHours = hours
            )
        )
    }

    // --------------------------------------------------
    // 🔹 NOUVEAU : MAJ PAUSE DÉJEUNER UNIQUEMENT
    // --------------------------------------------------
    suspend fun updateLunchBreak(
        enabled: Boolean,
        outside: Boolean,
        durationMin: Int
    ) {
        val current = settingsDao.getSettings() ?: SettingsEntity()
        settingsDao.insert(
            current.copy(
                lunchBreakEnabled = enabled,
                lunchBreakOutside = outside,
                lunchBreakDurationMin = durationMin
            )
        )
    }
}
