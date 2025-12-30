package com.example.badgeuse_auto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.badgeuse_auto.data.*
import com.example.badgeuse_auto.location.GeofenceManager
import com.example.badgeuse_auto.location.GeofenceService
import com.example.badgeuse_auto.ui.location.LocationViewModel
import com.example.badgeuse_auto.ui.navigation.RootNav
import com.example.badgeuse_auto.ui.theme.AppStyle
import com.example.badgeuse_auto.ui.theme.BadgeuseTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {

    private val db by lazy { PresenceDatabase.getDatabase(this) }

    private val repo by lazy {
        PresenceRepository(
            presenceDao = db.presenceDao(),
            workLocationDao = db.workLocationDao(),
            settingsDao = db.settingsDao()
        )
    }

    private val presenceVM: PresenceViewModel by viewModels {
        PresenceViewModelFactory(repo)
    }

    private val settingsVM: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsRepository(db.settingsDao()))
    }

    private val locationVM: LocationViewModel by viewModels()

    private lateinit var geofenceManager: GeofenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        geofenceManager = GeofenceManager(this)

        requestLocationPermissions()

        setContent {
            val settings by settingsVM.settingsFlow
                .collectAsState(initial = SettingsEntity())

            BadgeuseTheme(
                style = AppStyle.valueOf(settings.appStyle),
                darkTheme = when (settings.themeMode) {
                    ThemeMode.DARK -> true
                    ThemeMode.LIGHT -> false
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                RootNav(
                    presenceViewModel = presenceVM,
                    settingsViewModel = settingsVM,
                    locationViewModel = locationVM
                )
            }
        }
    }

    private fun requestLocationPermissions() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQ_FINE
            )
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQ_BACKGROUND
            )
            return
        }

        Log.d("GEOFENCE", "Permissions OK")

        startGeofenceService()
        startGeofences()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) return
        requestLocationPermissions()
    }

    private fun startGeofenceService() {
        val intent = Intent(this, GeofenceService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun startGeofences() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                presenceVM.workLocations.collect { locations ->
                    Log.d("GEOFENCE", "Register ${locations.size} geofences")
                    geofenceManager.clearGeofences()
                    geofenceManager.registerGeofences(locations)
                }
            }
        }
    }

    companion object {
        private const val REQ_FINE = 1001
        private const val REQ_BACKGROUND = 1002
    }
}
