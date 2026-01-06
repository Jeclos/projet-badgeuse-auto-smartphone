package com.example.badgeuse_auto

import android.app.Application
import android.util.Log
import com.example.badgeuse_auto.location.GeofenceManager

class BadgeuseApp : Application() {

    lateinit var geofenceManager: GeofenceManager
        private set

    override fun onCreate() {
        super.onCreate()

        // ✅ UNE SEULE INSTANCE GLOBALE
        geofenceManager = GeofenceManager(this)

        Log.d("GEOFENCE", "✅ GeofenceManager initialisé (Application)")
    }
}

