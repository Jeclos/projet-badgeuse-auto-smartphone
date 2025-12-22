package com.example.badgeuse_auto.data

import kotlinx.coroutines.flow.Flow

class PresenceRepository(
    private val presenceDao: PresenceDao,
    private val workLocationDao: WorkLocationDao,
    private val dailySummaryDao: DailySummaryDao,
    private val settingsDao: SettingsDao
) {

    // ...

    fun getSettingsFlow() = settingsDao.getSettingsFlow()
    // --------------------------------------------------------------------

    suspend fun updateDailyWorkHours(hours: Int) {
        val settings = settingsDao.getSettings() ?: SettingsEntity()
        settingsDao.insert(settings.copy(dailyWorkHours = hours))
    }
    // --------------------------------------------------------------------
    val allPresences: Flow<List<PresenceEntry>> = presenceDao.getAllPresences()

    fun getPresencesBetween(from: Long, to: Long): Flow<List<PresenceEntry>> {
        return presenceDao.getBetween(from, to)
    }

    suspend fun insertPresence(entry: PresenceEntry) {
        // 1) enregistrer la présence
        presenceDao.insert(entry)

        // 2) recalculer automatiquement le résumé du jour
        val dayStart = computeStartOfDay(entry.timestamp)
        recomputeAndSaveDailySummary(dayStart)
    }

    suspend fun updatePresence(entry: PresenceEntry) {
        presenceDao.update(entry)
        recomputeAndSaveDailySummary(computeStartOfDay(entry.timestamp))
    }

    suspend fun deletePresence(entry: PresenceEntry) {
        presenceDao.delete(entry)
        recomputeAndSaveDailySummary(computeStartOfDay(entry.timestamp))
    }

    // --------------------------------------------------------------------
    // DAILY SUMMARY API
    // --------------------------------------------------------------------

    fun getAllSummaries(): Flow<List<DailyWorkSummary>> =
        dailySummaryDao.getAllSummaries()

    fun getSummariesBetween(from: Long, to: Long): Flow<List<DailyWorkSummary>> =
        dailySummaryDao.getSummariesBetween(from, to)

    suspend fun getSummariesBetweenList(from: Long, to: Long): List<DailyWorkSummary> =
        dailySummaryDao.getSummariesBetweenList(from, to)


    // Recompute full day summary (called after each insert)
    suspend fun recomputeAndSaveDailySummary(dayStart: Long) {
        val dayEnd = dayStart + 24 * 60 * 60 * 1000L - 1

        // Charger toutes les entrées du jour
        val entries = presenceDao.getBetweenList(dayStart, dayEnd)

        // Calculer les minutes
        val totalMinutes = computeTotalMinutesFromEntries(entries)

        // Sauvegarder/updater le résumé
        val summary = DailyWorkSummary(dayStart = dayStart, totalMinutes = totalMinutes)
        dailySummaryDao.upsert(summary)
    }


    // --------------------------------------------------------------------
    // WORK LOCATION API
    // --------------------------------------------------------------------
    suspend fun saveWorkLocation(location: WorkLocationEntity) {
        workLocationDao.saveLocation(location)
    }

    suspend fun getWorkLocation(): WorkLocationEntity? {
        return workLocationDao.getLocation()
    }

    suspend fun deleteWorkLocation() {
        workLocationDao.deleteLocation()
    }


    // --------------------------------------------------------------------
    // INTERNAL HELPERS
    // --------------------------------------------------------------------

    private fun computeStartOfDay(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun computeTotalMinutesFromEntries(entries: List<PresenceEntry>): Int {
        var totalMinutes = 0
        var lastEntryTime: Long? = null
        var lastType: String? = null

        // Assurer un tri correct
        for (entry in entries.sortedBy { it.timestamp }) {

            if (entry.type == "ENTREE") {
                lastEntryTime = entry.timestamp
                lastType = "ENTREE"
            }
            else if (entry.type == "SORTIE" && lastType == "ENTREE" && lastEntryTime != null) {
                val diff = entry.timestamp - lastEntryTime
                totalMinutes += (diff / 60000).toInt()
                lastEntryTime = null
                lastType = "SORTIE"
            }
        }

        // Si entrée sans sortie → compter jusqu'à la fin du jour
        if (lastType == "ENTREE" && lastEntryTime != null) {
            val dayEnd = computeStartOfDay(lastEntryTime) + 24*60*60*1000L - 1
            val diff = dayEnd - lastEntryTime
            if (diff > 0) totalMinutes += (diff / 60000).toInt()
        }

        return totalMinutes
    }
}
