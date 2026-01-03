package com.example.badgeuse_auto.location

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.badgeuse_auto.R
import com.google.android.gms.location.*

class GeofenceService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // 🔹 Thread dédié GPS (critique sur téléphone réel)
    private lateinit var handlerThread: HandlerThread

    override fun onCreate() {
        super.onCreate()

        Log.e("SERVICE_TEST", "GeofenceService CREATED")

        // 🔒 Obligatoire AVANT toute requête GPS
        startForeground(NOTIF_ID, createNotification())

        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        // 🔹 Thread séparé pour éviter le gel du mainLooper
        handlerThread = HandlerThread("GPS_THREAD")
        handlerThread.start()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation
                Log.e(
                    "GPS_REAL",
                    "Location update: ${loc?.latitude}, ${loc?.longitude}"
                )
            }
        }

        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {

        val fineGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val bgGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else true

        if (!fineGranted || !bgGranted) {
            Log.e(
                "GEOFENCE",
                "Permissions GPS manquantes (fine=$fineGranted, bg=$bgGranted)"
            )
            return
        }

        // ⚠️ setWaitForAccurateLocation SUPPRIMÉ (bloque le GPS réel)
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            30_000L
        )
            .setMinUpdateIntervalMillis(15_000L)
            .build()

        fusedClient.requestLocationUpdates(
            request,
            locationCallback,
            handlerThread.looper
        )

        Log.e("GEOFENCE", "High accuracy GPS started (foreground)")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("SERVICE_TEST", "GeofenceService START COMMAND")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.e("SERVICE_TEST", "GeofenceService DESTROYED")

        fusedClient.removeLocationUpdates(locationCallback)
        handlerThread.quitSafely()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "geofence_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Badgeuse automatique",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Badgeuse active")
            .setContentText("Surveillance des zones")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIF_ID = 100
    }
}
