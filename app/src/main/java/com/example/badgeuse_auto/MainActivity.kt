package com.example.badgeuse_auto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import com.example.badgeuse_auto.data.*
import com.example.badgeuse_auto.location.GeofenceService
import com.example.badgeuse_auto.ui.location.LocationViewModel
import com.example.badgeuse_auto.ui.navigation.RootNav
import com.example.badgeuse_auto.ui.theme.AppStyle
import com.example.badgeuse_auto.ui.theme.BadgeuseTheme

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            )
            startActivity(intent)
            return
        }

        startGeofenceService()
    }

    private fun startGeofenceService() {
        val intent = Intent(this, GeofenceService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    companion object {
        private const val REQ_FINE = 1001
    }
}
