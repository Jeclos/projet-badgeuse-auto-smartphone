package com.example.badgeuse_auto.data
import kotlinx.coroutines.flow.Flow
class SettingsRepository(private val settingsDao: SettingsDao) {


    suspend fun getSettings(): SettingsEntity {
        return settingsDao.getSettings() ?: SettingsEntity()
    }

    suspend fun saveSettings(settings: SettingsEntity) {
        settingsDao.insert(settings)
    }

    fun getSettingsFlow(): Flow<SettingsEntity?> {
        return settingsDao.getSettingsFlow()
    }
    suspend fun updateDailyWorkHours(hours: Int) {
        val current = settingsDao.getSettings() ?: SettingsEntity()
        settingsDao.insert(current.copy(dailyWorkHours = hours))
    }
}

