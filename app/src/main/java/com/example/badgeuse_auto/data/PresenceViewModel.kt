package com.example.badgeuse_auto.data

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.min
import com.example.badgeuse_auto.domain.WorkTimeCalculator
import com.example.badgeuse_auto.location.GeofenceManager


class PresenceViewModel(
    private val repository: PresenceRepository,
    private val geofenceManager: GeofenceManager

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

    /** Tous les lieux (settings + stats) */
    val allWorkLocations: StateFlow<List<WorkLocationEntity>> =
        repository.getAllWorkLocations().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Lieux actifs uniquement (badgeage) */
    val activeWorkLocations: StateFlow<List<WorkLocationEntity>> =
        repository.getActiveWorkLocations().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    init {
        observeWorkLocationsForGeofences()
    }
    /* ---------------- WORK LOCATIONS CRUD ---------------- */
    fun onBadgeModeChanged(newMode: BadgeMode) {
        viewModelScope.launch {

            repository.saveSettings(
                settings.value.copy(badgeMode = newMode)
            )

            if (newMode == BadgeMode.DEPOT) {

                val allLocations = repository.getAllWorkLocationsOnce()

                if (allLocations.isEmpty()) return@launch

                // On garde UN seul actif (le premier)
                val keepActiveId = allLocations.first().id

                allLocations.forEach { location ->
                    repository.updateWorkLocation(
                        location.copy(
                            isActive = location.id == keepActiveId
                        )
                    )
                }
            }
        }
    }

    fun addWorkLocation(
        name: String,
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {

            val badgeMode = repository.getBadgeMode()

            if (badgeMode == BadgeMode.DEPOT) {
                // 👉 en mode dépôt : désactiver tous les autres
                val allLocations = repository.getAllWorkLocationsOnce()
                allLocations.forEach { loc ->
                    if (loc.isActive) {
                        repository.updateWorkLocation(loc.copy(isActive = false))
                    }
                }
            }

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

    fun updateWorkLocation(
        location: WorkLocationEntity,
        name: String,
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            repository.updateWorkLocation(
                location.copy(
                    name = name,
                    latitude = latitude,
                    longitude = longitude
                )
            )
        }
    }

    /** Désactivation / activation  */
    fun setWorkLocationActive(
        location: WorkLocationEntity,
        active: Boolean
    ) {
        viewModelScope.launch {

            val badgeMode = repository.getBadgeMode()

            if (badgeMode == BadgeMode.DEPOT || badgeMode == BadgeMode.HOME_TRAVEL) {

                val allLocations = repository.getAllWorkLocationsOnce()

                if (active) {
                    // 👉 un seul actif autorisé
                    allLocations.forEach { loc ->
                        val shouldBeActive = loc.id == location.id
                        if (loc.isActive != shouldBeActive) {
                            repository.updateWorkLocation(
                                loc.copy(isActive = shouldBeActive)
                            )
                        }
                    }
                } else {
                    // 👉 désactivation autorisée
                    repository.updateWorkLocation(
                        location.copy(isActive = false)
                    )
                }

                return@launch
            }

            // MODE OFFICE
            repository.updateWorkLocation(
                location.copy(isActive = active)
            )
        }
    }





    fun deleteWorkLocation(location: WorkLocationEntity) {
        viewModelScope.launch {
            repository.deleteWorkLocation(location)
        }
    }

    /* ---------------- STATS (présence en cours incluse) ---------------- */

    private fun startOfWorkDay(
        time: Long,
        startHour: Int,
        startMinute: Int
    ): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Si on est AVANT l’heure de début → journée précédente
        if (time < cal.timeInMillis) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }

        return cal.timeInMillis
    }



    fun dailyStatsBetween(
        from: Long,
        to: Long,
        locationName: String? = null
    ): Flow<List<DailyStat>> =
        combine(allPresences, allWorkLocations, settings) { presences, locations, settings ->

            val locationMap = locations.associate { it.id to it.name }
            val result = mutableMapOf<Pair<Long, Long>, Long>()

            presences.forEach { presence ->

                if (presence.exitTime == null) return@forEach

                val locationLabel =
                    locationMap[presence.workLocationId] ?: "Inconnu"

                // 🔎 FILTRE EMPLACEMENT
                if (locationName != null && locationLabel != locationName) {
                    return@forEach
                }

                val dayStart =
                    WorkTimeCalculator.startOfWorkDay(
                        presence.enterTime,
                        settings.depotStartHour,
                        settings.depotStartMinute
                    )

// ⚠️ On garde la présence si ELLE CHEVAUCHE la période
                val presenceStart = presence.enterTime
                val presenceEnd = presence.exitTime ?: presence.enterTime

                if (presenceEnd < from || presenceStart > to) return@forEach

                val minutes =
                    WorkTimeCalculator.computePayableMinutes(
                        presence,
                        settings
                    )

                val key = dayStart to presence.workLocationId
                result[key] = (result[key] ?: 0L) + minutes
            }


            result.map { (key, minutes) ->
                DailyStat(
                    dayStart = key.first,
                    totalMinutes = minutes,
                    workLocationName =
                        locationMap[key.second] ?: "Inconnu"
                )
            }.sortedBy { it.dayStart }
        }



    fun totalMinutesToday(): Flow<Long> =
        dailyStatsBetween(startOfToday(), endOfToday())
            .map { stats -> stats.sumOf { it.totalMinutes } }

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
                    exitType = "MANUAL",
                    locked = true
                )
            )
        }
    }

    /* ---------------- PRESENCE CRUD ---------------- */

    fun updatePresence(entry: PresenceEntity) =
        viewModelScope.launch {
            repository.updatePresence(entry)
        }

    fun deletePresence(entry: PresenceEntity) =
        viewModelScope.launch {
            repository.deletePresence(entry)
        }

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

    val settingsFlow = repository.settings
    @SuppressLint("MissingPermission")
    private fun observeWorkLocationsForGeofences() {
        viewModelScope.launch {
            combine(
                repository.getAllWorkLocations(),
                repository.settings
            ) { locations, settings ->
                locations to settings
            }.collect { (locations, settings) ->
                geofenceManager.rebuildAllWithSettings(locations, settings)
            }
        }
    }


}
