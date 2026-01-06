package com.example.badgeuse_auto.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.badgeuse_auto.location.GeofenceManager

class PresenceViewModelFactory(
    private val repository: PresenceRepository,
    private val geofenceManager: GeofenceManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PresenceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PresenceViewModel(
                repository = repository,
                geofenceManager = geofenceManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
