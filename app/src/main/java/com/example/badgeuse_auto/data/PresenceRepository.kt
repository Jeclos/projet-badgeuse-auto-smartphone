package com.example.badgeuse_auto.data

import kotlinx.coroutines.flow.Flow

class PresenceRepository(
    private val presenceDao: PresenceDao,
    private val workLocationDao: WorkLocationDao
) {

    // ----------- PRESENCES -----------
    val allPresences: Flow<List<PresenceEntry>> = presenceDao.getAllPresences()

    fun getPresencesBetween(from: Long, to: Long): Flow<List<PresenceEntry>> {
        return presenceDao.getBetween(from, to)
    }

    suspend fun addPresence(entry: PresenceEntry) {
        presenceDao.insert(entry)
    }

    // ----------- WORK LOCATION -----------
    suspend fun saveWorkLocation(location: WorkLocationEntity) {
        workLocationDao.saveLocation(location)
    }

    suspend fun getWorkLocation(): WorkLocationEntity? {
        return workLocationDao.getLocation()
    }

    suspend fun deleteWorkLocation() {
        workLocationDao.deleteLocation()
    }
}
