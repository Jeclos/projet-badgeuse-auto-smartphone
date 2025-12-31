package com.example.badgeuse_auto.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.*

class PresenceViewModel(
    private val repository: PresenceRepository
) : ViewModel() {

    /* ---------------- SETTINGS ---------------- */

    val settings: StateFlow<SettingsEntity> =
        repository.settings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsEntity()
        )

    /* ---------------- PRESENCES ---------------- */

    val allPresences: StateFlow<List<PresenceEntity>> =
        repository.getAllPresences().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /* ---------------- WORK LOCATIONS ---------------- */

    val workLocations: StateFlow<List<WorkLocationEntity>> =
        repository.getActiveWorkLocations().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addWorkLocation(name: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.addWorkLocation(
                WorkLocationEntity(
                    name = name,
                    latitude = latitude,
                    longitude = longitude,
                    isActive = true
                )
            )
        }
    }

    fun deleteWorkLocation(location: WorkLocationEntity) {
        viewModelScope.launch {
            repository.deleteWorkLocation(location)
        }
    }

    fun setWorkLocationActive(location: WorkLocationEntity, active: Boolean) {
        viewModelScope.launch {
            repository.updateWorkLocation(
                location.copy(isActive = active)
            )
        }
    }

    /* ---------------- STATS (PRESENCE EN COURS INCLUSE) ---------------- */

    fun dailyStatsBetween(from: Long, to: Long): Flow<List<DailyStat>> =
        combine(allPresences, workLocations) { presences, locations ->

            if (from == 0L || to == 0L) return@combine emptyList()

            val locationMap = locations.associate { it.id to it.name }
            val result = mutableMapOf<Pair<Long, Long>, Long>()
            val now = System.currentTimeMillis()

            presences.forEach { p ->

                val end = p.exitTime ?: now
                var current = p.enterTime

                while (current < end) {
                    val dayStart = startOfDay(current)
                    val dayEnd = dayStart + 86_400_000L
                    val sliceEnd = minOf(dayEnd, end)

                    val minutes = (sliceEnd - current) / 60_000
                    val key = dayStart to p.workLocationId

                    result[key] = (result[key] ?: 0L) + minutes
                    current = sliceEnd
                }
            }

            result
                .filter { it.key.first in from..to }
                .map { (key, minutes) ->
                    DailyStat(
                        dayStart = key.first,
                        totalMinutes = minutes,
                        workLocationName =
                            locationMap[key.second] ?: "Inconnu"
                    )
                }
                .sortedBy { it.dayStart }
        }

    fun totalMinutesToday(): Flow<Long> =
        dailyStatsBetween(startOfToday(), endOfToday())
            .map { list -> list.sumOf { it.totalMinutes } }

    /* ---------------- MANUAL ---------------- */

    fun manualEntry(workLocationId: Long) {
        viewModelScope.launch {
            repository.insertPresence(
                PresenceEntity(
                    workLocationId = workLocationId,
                    enterTime = System.currentTimeMillis(),
                    enterType = "MANUAL"
                )
            )
        }
    }

    fun manualExit() {
        viewModelScope.launch {
            val current = repository.getCurrentPresence() ?: return@launch
            repository.updatePresence(
                current.copy(
                    exitTime = System.currentTimeMillis(),
                    exitType = "MANUAL"
                )
            )
        }
    }

    /* ---------------- CRUD ---------------- */

    fun updatePresence(entry: PresenceEntity) =
        viewModelScope.launch { repository.updatePresence(entry) }

    fun deletePresence(entry: PresenceEntity) =
        viewModelScope.launch { repository.deletePresence(entry) }

    /* ---------------- UTILS ---------------- */

    private fun startOfDay(time: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun startOfToday(): Long =
        startOfDay(System.currentTimeMillis())

    private fun endOfToday(): Long =
        startOfToday() + 86_399_999L
}
