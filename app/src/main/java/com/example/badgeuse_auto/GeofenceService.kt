package com.example.badgeuse_auto.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.badgeuse_auto.R

class GeofenceService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "geofence_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                "Badgeuse automatique",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Badgeuse active")
            .setContentText("Détection de présence en cours")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // ✅ icône sûre
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIF_ID = 100
    }
}
