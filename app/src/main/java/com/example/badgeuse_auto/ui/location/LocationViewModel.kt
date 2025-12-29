package com.example.badgeuse_auto.ui.location

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class LocationUiState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null,
    val error: String? = null
)

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _location = MutableStateFlow(LocationUiState())
    val location: StateFlow<LocationUiState> = _location

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            _location.value = LocationUiState(
                latitude = loc.latitude,
                longitude = loc.longitude,
                accuracy = loc.accuracy
            )
        }
    }

    fun startLocationUpdates() {

        if (
            ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _location.value = LocationUiState(error = "Permission GPS manquante")
            return
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5_000 // toutes les 5 secondes
        ).build()

        fusedClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        fusedClient.removeLocationUpdates(locationCallback)
    }

    override fun onCleared() {
        stopLocationUpdates()
        super.onCleared()
    }
}
