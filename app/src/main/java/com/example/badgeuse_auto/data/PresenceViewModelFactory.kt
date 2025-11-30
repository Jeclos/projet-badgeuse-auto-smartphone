package com.example.badgeuse_auto.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PresenceViewModelFactory(private val repository: PresenceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PresenceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PresenceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
