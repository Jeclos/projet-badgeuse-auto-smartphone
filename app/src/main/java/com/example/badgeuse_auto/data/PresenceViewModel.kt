package com.example.badgeuse_auto.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class PresenceViewModel(
    private val repository: PresenceRepository
) : ViewModel() {

    // --------------------------------------------------------------------
    // PRESENCES FLOW
    // --------------------------------------------------------------------
    val allPresences: StateFlow<List<PresenceEntry>> =
        repository.allPresences
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // --------------------------------------------------------------------
    // DAILY SUMMARY FLOW
    // --------------------------------------------------------------------
    val allDailySummaries: StateFlow<List<DailyWorkSummary>> =
        repository.getAllSummaries()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun summariesBetween(from: Long, to: Long): Flow<List<DailyWorkSummary>> =
        repository.getSummariesBetween(from, to)

    suspend fun loadSummariesBetween(from: Long, to: Long): List<DailyWorkSummary> =
        repository.getSummariesBetweenList(from, to)

    // --------------------------------------------------------------------
    // SETTINGS FLOW
    // --------------------------------------------------------------------
    val settings: StateFlow<SettingsEntity> =
        repository.getSettingsFlow()
            .map { it ?: SettingsEntity() }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                SettingsEntity()
            )

    // --------------------------------------------------------------------
    // WORK LOCATION FLOW  ✅ (AJOUTÉ / FIX)
    // --------------------------------------------------------------------
    private val _workLocation = MutableStateFlow<WorkLocationEntity?>(null)
    val workLocation: StateFlow<WorkLocationEntity?> = _workLocation

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _workLocation.value = repository.getWorkLocation()
        }
    }

    // --------------------------------------------------------------------
    // MANUAL EVENT
    // --------------------------------------------------------------------
    fun manualEvent(type: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = performRecordIfAllowed(type)

            val message = when {
                !hasWorkLocation() -> "Aucun lieu configuré"
                !success -> "Opération impossible : double $type"
                else -> "$type enregistrée"
            }

            withContext(Dispatchers.Main) {
                callback(success, message)
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
            val success = performRecordIfAllowed(type)

            val message = when {
                !hasWorkLocation() -> "Aucun lieu configuré"
                !success -> "Opération impossible : double $type"
                else -> "$type automatique enregistrée"
            }

            withContext(Dispatchers.Main) {
                callback(success, message)
            }
        }
    }

    // --------------------------------------------------------------------
    // TEMPS JOURNALIER EN DIRECT
    // --------------------------------------------------------------------
    fun totalMinutesToday(): StateFlow<Int> {

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L

        return repository.getPresencesBetween(startOfDay, endOfDay)
            .map { entries ->
                var totalMinutes = 0
                var lastEntryTime: Long? = null

                for (entry in entries.sortedBy { it.timestamp }) {
                    when (entry.type) {
                        "ENTREE" -> lastEntryTime = entry.timestamp
                        "SORTIE" -> {
                            lastEntryTime?.let {
                                totalMinutes += ((entry.timestamp - it) / 60000).toInt()
                                lastEntryTime = null
                            }
                        }
                    }
                }

                // entrée en cours
                lastEntryTime?.let {
                    totalMinutes += ((System.currentTimeMillis() - it) / 60000).toInt()
                }

                totalMinutes
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )
    }

    // --------------------------------------------------------------------
    // LOGIQUE D’ENREGISTREMENT
    // --------------------------------------------------------------------
    private fun hasWorkLocation(): Boolean =
        _workLocation.value != null

    private suspend fun performRecordIfAllowed(type: String): Boolean {

        val workLocation = repository.getWorkLocation()
        if (workLocation == null) return false

        val last = allPresences.value.firstOrNull()
        if (last != null && last.type == type) return false

        val now = System.currentTimeMillis()

        repository.insertPresence(
            PresenceEntry(
                timestamp = now,
                type = type,
                locationName = workLocation.name // ✅ vrai nom
            )
        )
        return true
    }


    // --------------------------------------------------------------------
    // WORK LOCATION API
    // --------------------------------------------------------------------
    suspend fun refreshWorkLocation(): WorkLocationEntity? {
        val loc = repository.getWorkLocation()
        _workLocation.value = loc
        return loc
    }

    fun saveWorkLocation(loc: WorkLocationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveWorkLocation(loc)
            _workLocation.value = loc
        }
    }

    fun getWorkLocation(): WorkLocationEntity? =
        _workLocation.value
}
