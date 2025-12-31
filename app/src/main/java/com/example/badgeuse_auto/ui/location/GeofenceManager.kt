package com.example.badgeuse_auto.location

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.badgeuse_auto.WorkGeofenceReceiver
import com.example.badgeuse_auto.data.WorkLocationEntity
import com.google.android.gms.location.*

class GeofenceManager(
    private val context: Context
) {

    private val geofencingClient =
        LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, WorkGeofenceReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE // 🔥 OBLIGATOIRE
        )
    }

    private fun buildGeofence(location: WorkLocationEntity): Geofence =
        Geofence.Builder()
            .setRequestId(location.id.toString())
            .setCircularRegion(
                location.latitude,
                location.longitude,
                150f
            )
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

    fun registerGeofences(locations: List<WorkLocationEntity>) {

        val fineGranted =
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val backgroundGranted =
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted || !backgroundGranted) {
            Log.e("GEOFENCE", "❌ Permissions location insuffisantes")
            return
        }

        val geofences = locations
            .filter { it.isActive }
            .map { buildGeofence(it) }

        if (geofences.isEmpty()) {
            Log.w("GEOFENCE", "⚠ Aucun geofence actif")
            return
        }

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()

        geofencingClient
            .addGeofences(request, geofencePendingIntent)
            .addOnSuccessListener {
                Log.d("GEOFENCE", "✅ Geofences enregistrées")
            }
            .addOnFailureListener { e ->
                Log.e("GEOFENCE", "❌ Erreur ajout geofences", e)
            }
    }

    fun clearGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)
    }
}
