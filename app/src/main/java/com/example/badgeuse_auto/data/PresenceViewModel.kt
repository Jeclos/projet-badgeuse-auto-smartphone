package com.example.badgeuse_auto.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class PresenceViewModel(private val repository: PresenceRepository) : ViewModel() {

    // --------------------------------------------------------------------
    // PRESENCES FLOW
    // --------------------------------------------------------------------
    val allPresences: StateFlow<List<PresenceEntry>> =
        repository.allPresences
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --------------------------------------------------------------------
    // DAILY SUMMARY FLOW
    // --------------------------------------------------------------------
    val allDailySummaries: StateFlow<List<DailyWorkSummary>> =
        repository.getAllSummaries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun summariesBetween(from: Long, to: Long): Flow<List<DailyWorkSummary>> {
        return repository.getSummariesBetween(from, to)
    }

    suspend fun loadSummariesBetween(from: Long, to: Long): List<DailyWorkSummary> {
        return repository.getSummariesBetweenList(from, to)
    }


    // --------------------------------------------------------------------
    // SETTINGS FLOW (AJOUTÉ)
    // --------------------------------------------------------------------
    val settings: StateFlow<SettingsEntity> =
        repository.getSettingsFlow()
            .map { it ?: SettingsEntity() }   // <-- convertit en non-nullable
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                SettingsEntity()              // valeur initiale
            )


    // --------------------------------------------------------------------
    // MANUAL EVENT (boutons Entrée / Sortie)
    // --------------------------------------------------------------------
    fun manualEvent(type: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = performRecordIfAllowed(type)

            val message = when {
                !hasWorkLocation() -> "Aucun lieu configuré"
                !result -> "Opération impossible : double $type"
                else -> "$type enregistrée"
            }

            withContext(Dispatchers.Main) {
                callback(result, message)
            }
        }
    }

    fun updatePresence(entry: PresenceEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePresence(entry)
        }
    }

    fun deletePresence(entry: PresenceEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePresence(entry)
        }
    }

    // --------------------------------------------------------------------
    // AUTO EVENT (géofence)
    // --------------------------------------------------------------------
    fun autoEvent(type: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = performRecordIfAllowed(type)

            val message = when {
                !hasWorkLocation() -> "Aucun lieu configuré"
                !result -> "Opération impossible : double $type"
                else -> "$type automatique enregistrée"
            }

            withContext(Dispatchers.Main) {
                callback(result, message)
            }
        }
    }


    // --------------------------------------------------------------------
    // TEMPS JOURNALIER EN DIRECT
    // --------------------------------------------------------------------
    fun totalMinutesToday(): StateFlow<Int> {

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L

        return repository.getPresencesBetween(startOfDay, endOfDay)
            .map { entries ->
                var totalMinutes = 0
                var lastEntryTime: Long? = null
                var lastType: String? = null

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

                // entrée en cours
                if (lastType == "ENTREE" && lastEntryTime != null) {
                    val now = System.currentTimeMillis()
                    val diff = now - lastEntryTime
                    totalMinutes += (diff / 60000).toInt()
                }

                totalMinutes
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    }


    // --------------------------------------------------------------------
    // LOGIQUE D’ENREGISTREMENT
    // --------------------------------------------------------------------
    private suspend fun hasWorkLocation(): Boolean {
        return repository.getWorkLocation() != null
    }

    private suspend fun performRecordIfAllowed(type: String): Boolean {

        if (!hasWorkLocation()) return false

        val last = allPresences.value.firstOrNull()

        if (last != null && last.type == type) return false

        val now = System.currentTimeMillis()

        repository.insertPresence(
            PresenceEntry(
                timestamp = now,
                type = type,
                locationName = "WORK"
            )
        )
        return true
    }


    // --------------------------------------------------------------------
    // WORK LOCATION API
    // --------------------------------------------------------------------
    suspend fun refreshWorkLocation() = repository.getWorkLocation()

    suspend fun saveWorkLocation(loc: WorkLocationEntity) {
        repository.saveWorkLocation(loc)
    }

    suspend fun getWorkLocation() = repository.getWorkLocation()
}
