package com.example.badgeuse_auto.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import java.util.*
class PresenceViewModel(private val repository: PresenceRepository) : ViewModel() {

    val allPresences: StateFlow<List<PresenceEntry>> =
        repository.allPresences
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addPresence(type: String, locationName: String) {
        val entry = PresenceEntry(
            timestamp = System.currentTimeMillis(),
            type = type,
            locationName = locationName
        )
        viewModelScope.launch {
            repository.addPresence(entry)
        }
    }

    // ---------- WORK LOCATION ----------
    fun saveWorkLocation(location: WorkLocationEntity) {
        viewModelScope.launch {
            repository.saveWorkLocation(location)
        }
    }

    suspend fun refreshWorkLocation(): WorkLocationEntity? {
        return repository.getWorkLocation()
    }

    suspend fun getWorkLocation(): WorkLocationEntity? {
        return repository.getWorkLocation()
    }

    // ----------- STATS ----------
    fun getTodayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val end = cal.timeInMillis - 1
        return start to end
    }

    fun totalMinutesToday(): StateFlow<Long> {
        val (from, to) = getTodayRange()
        return repository.getPresencesBetween(from, to)
            .map { entries ->
                var totalMs = 0L
                var lastIn: Long? = null
                entries.forEach { e ->
                    if (e.type == "ENTREE") lastIn = e.timestamp
                    else if (e.type == "SORTIE" && lastIn != null) {
                        totalMs += (e.timestamp - lastIn!!)
                        lastIn = null
                    }
                }
                totalMs / 60000L
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)
    }
}
