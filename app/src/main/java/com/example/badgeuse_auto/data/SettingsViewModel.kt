package com.example.badgeuse_auto.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.badgeuse_auto.ui.theme.AppStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val settingsFlow = repository.observeSettings()

    suspend fun loadSettings(): SettingsEntity {
        return repository.getSettings()
    }

    // 🎨 STYLE
    fun setAppStyle(styleName: AppStyle) {
        viewModelScope.launch {
            repository.updateAppStyle(styleName)
        }
    }

    // 🌙 THEME MODE
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.updateThemeMode(mode)
        }
    }

    fun saveSettings(
        enterDistance: Int,
        exitDistance: Int,
        enterDelaySec: Int,
        exitDelaySec: Int,
        lunchEnabled: Boolean,
        lunchOutside: Boolean,
        lunchDurationMin: Int,
        employeeName: String,
        employeeAddress: String,
        employerName: String,
        employerAddress: String,
        city: String,

        depotStartHour: Int,
        depotStartMinute: Int,
        depotEndHour: Int,
        depotEndMinute: Int,
        depotAdjustMin: Int,

        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getSettings()

            repository.saveSettings(
                current.copy(
                    enterDistance = enterDistance,
                    exitDistance = exitDistance,
                    enterDelaySec = enterDelaySec,
                    exitDelaySec = exitDelaySec,

                    lunchBreakEnabled = lunchEnabled,
                    lunchBreakOutside = lunchOutside,
                    lunchBreakDurationMin = lunchDurationMin,

                    employeeName = employeeName,
                    employeeAddress = employeeAddress,
                    employerName = employerName,
                    employerAddress = employerAddress,
                    city = city,

                    depotStartHour = depotStartHour,
                    depotStartMinute = depotStartMinute,
                    depotEndHour = depotEndHour,
                    depotEndMinute = depotEndMinute,
                    depotDailyAdjustMin = depotAdjustMin
                )
            )

            withContext(Dispatchers.Main) { onDone() }
        }
    }


    fun setDailyWorkHours(hours: Int) {
        viewModelScope.launch {
            repository.updateDailyWorkHours(hours)
        }
    }

    fun setBadgeMode(mode: BadgeMode) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.saveSettings(
                current.copy(badgeMode = mode)
            )
        }
    }

}



