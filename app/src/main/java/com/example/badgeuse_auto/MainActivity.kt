package com.example.badgeuse_auto

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.badgeuse_auto.data.*
import com.example.badgeuse_auto.ui.navigation.RootNav
import com.example.badgeuse_auto.ui.theme.Badgeuse_AutoTheme
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    private val requestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions(locationPermissions, requestCode)

        // --------------------------------------------------
        // DATABASE UNIQUE
        // --------------------------------------------------
        val db = PresenceDatabase.getDatabase(this)

        // --------------------------------------------------
        // REPOSITORIES
        // --------------------------------------------------
        val presenceRepo = PresenceRepository(
            presenceDao = db.presenceDao(),
            workLocationDao = db.workLocationDao(),
            dailySummaryDao = db.dailySummaryDao(),
            settingsDao = db.settingsDao() // ✅ ICI
        )

        val settingsRepo = SettingsRepository(
            settingsDao = db.settingsDao() // ✅ ET ICI
        )

        // --------------------------------------------------
        // VIEWMODELS
        // --------------------------------------------------
        val presenceViewModel =
            PresenceViewModelFactory(presenceRepo)
                .create(PresenceViewModel::class.java)

        val settingsViewModel =
            SettingsViewModelFactory(settingsRepo)
                .create(SettingsViewModel::class.java)

        enableEdgeToEdge()

        // --------------------------------------------------
        // UI
        // --------------------------------------------------
        setContent {
            Badgeuse_AutoTheme {
                RootNav(
                    presenceViewModel = presenceViewModel,
                    settingsViewModel = settingsViewModel,

                )
            }
        }

        // --------------------------------------------------
        // GEOFENCE AU DÉMARRAGE
        // --------------------------------------------------
        lifecycleScope.launch {
            val loc = presenceViewModel.getWorkLocation()
            if (loc != null) {
                registerWorkGeofence(loc.latitude, loc.longitude)
            }
        }
    }

    // -------------------------------------------------------------
    private fun updateGeofence(vm: PresenceViewModel) {
        lifecycleScope.launch {
            val loc = vm.getWorkLocation()
            if (loc != null) {
                registerWorkGeofence(loc.latitude, loc.longitude)
            } else {
                Log.w("GEOFENCE", "Aucun lieu enregistré")
            }
        }
    }

    // -------------------------------------------------------------
    private suspend fun registerWorkGeofence(latitude: Double, longitude: Double) {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("GEOFENCE", "Permissions manquantes")
            return
        }

        val geofencingClient = LocationServices.getGeofencingClient(this)

        val geofence = Geofence.Builder()
            .setRequestId("WORK_ZONE")
            .setCircularRegion(latitude, longitude, 150f)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(this, WorkGeofenceReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        geofencingClient.addGeofences(request, pendingIntent)
    }
}

