package com.example.badgeuse_auto.location

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.badgeuse_auto.R
import com.google.android.gms.location.*

class GeofenceService : Service() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()

        startForeground(NOTIF_ID, createNotification())
        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation
                Log.d(
                    "GEOFENCE",
                    "Location update: ${loc?.latitude}, ${loc?.longitude}"
                )
            }
        }

        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            30_000L
        )
            .setMinUpdateIntervalMillis(15_000L)
            .setWaitForAccurateLocation(true)
            .build()

        fusedClient.requestLocationUpdates(
            request,
            locationCallback,
            mainLooper
        )

        Log.d("GEOFENCE", "High accuracy GPS started")
    }

    override fun onDestroy() {
        fusedClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
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
