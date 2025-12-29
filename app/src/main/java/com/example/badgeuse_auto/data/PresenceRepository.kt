package com.example.badgeuse_auto.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class PresenceRepository(
    private val presenceDao: PresenceDao,
    private val workLocationDao: WorkLocationDao,
    private val settingsDao: SettingsDao
) {

    /* ---------------- SETTINGS ---------------- */

    val settings: Flow<SettingsEntity> =
        settingsDao.getSettingsFlow()
            .filterNotNull()

    suspend fun saveSettings(settings: SettingsEntity) {
        settingsDao.insertOrUpdate(settings)
    }

    /* ---------------- PRESENCES ---------------- */

    fun getAllPresences(): Flow<List<PresenceEntity>> =
        presenceDao.getAllPresences()

    suspend fun getCurrentPresence(): PresenceEntity? =
        presenceDao.getCurrentPresence()

    suspend fun insertPresence(entry: PresenceEntity): Long =
        presenceDao.insert(entry)

    suspend fun updatePresence(entry: PresenceEntity) =
        presenceDao.update(entry)

    suspend fun deletePresence(entry: PresenceEntity) =
        presenceDao.delete(entry)

    suspend fun getPresencesBetween(
        start: Long,
        end: Long
    ): List<PresenceEntity> =
        presenceDao.getPresencesBetween(start, end)

    /* ---------------- WORK LOCATIONS ---------------- */

    fun getAllWorkLocations(): Flow<List<WorkLocationEntity>> =
        workLocationDao.getActiveLocations()

    suspend fun getAllWorkLocationsOnce(): List<WorkLocationEntity> =
        workLocationDao.getActiveLocationsOnce()

    suspend fun addWorkLocation(location: WorkLocationEntity): Long =
        workLocationDao.insert(location)

    suspend fun updateWorkLocation(location: WorkLocationEntity) =
        workLocationDao.update(location)

    suspend fun deleteWorkLocation(location: WorkLocationEntity) =
        workLocationDao.delete(location)

    /* ---------------- AUTO GEOFENCE ---------------- */

    suspend fun autoEvent(
        isEnter: Boolean,
        workLocation: WorkLocationEntity
    ): String {

        val now = System.currentTimeMillis()
        val currentPresence = getCurrentPresence()

        return if (isEnter) {
            if (currentPresence != null) {
                "Déjà présent sur un lieu"
            } else {
                insertPresence(
                    PresenceEntity(
                        workLocationId = workLocation.id,
                        enterTime = now,
                        enterType = "AUTO"
                    )
                )
                "Arrivée automatique : ${workLocation.name}"
            }
        } else {
            if (currentPresence == null) {
                "Aucune présence en cours"
            } else {
                updatePresence(
                    currentPresence.copy(
                        exitTime = now,
                        exitType = "AUTO"
                    )
                )
                "Départ automatique : ${workLocation.name}"
            }
        }
    }
}
