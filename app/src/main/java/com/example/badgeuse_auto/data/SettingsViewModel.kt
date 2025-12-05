package com.example.badgeuse_auto.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    suspend fun loadSettings(): SettingsEntity {
        return repository.getSettings()
    }

    fun saveSettings(
        enterDistance: Int,
        exitDistance: Int,
        enterDelaySec: Int,
        exitDelaySec: Int,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(
                SettingsEntity(
                    id = 1,
                    enterDistance = enterDistance,
                    exitDistance = exitDistance,
                    enterDelaySec = enterDelaySec,
                    exitDelaySec = exitDelaySec
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
}
