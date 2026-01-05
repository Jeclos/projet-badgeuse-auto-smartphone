package com.example.badgeuse_auto

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.badgeuse_auto.data.PresenceDatabase
import com.example.badgeuse_auto.data.PresenceRepository
import com.example.badgeuse_auto.location.GeofenceManager
import kotlinx.coroutines.*

class BadgeuseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val fineGranted =
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val backgroundGranted =
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted || !backgroundGranted) {
            Log.w("GEOFENCE", "⚠ Permissions non accordées au démarrage")
            return
        }

        val db = PresenceDatabase.getDatabase(this)
        val repo = PresenceRepository(
            presenceDao = db.presenceDao(),
            workLocationDao = db.workLocationDao(),
            settingsDao = db.settingsDao()
        )

        val geofenceManager = GeofenceManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            val locations = repo.getAllWorkLocationsOnce()
            geofenceManager.rebuildAll(locations)
        }

    }
}
