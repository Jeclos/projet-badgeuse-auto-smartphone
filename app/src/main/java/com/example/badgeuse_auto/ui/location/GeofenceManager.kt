package com.example.badgeuse_auto.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.example.badgeuse_auto.WorkGeofenceReceiver
import com.example.badgeuse_auto.data.WorkLocationEntity
import com.google.android.gms.location.*

class GeofenceManager(
    private val context: Context
) {

    private val geofencingClient =
        LocationServices.getGeofencingClient(context)

    // 🔔 PendingIntent UNIQUE
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, WorkGeofenceReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // 🔨 Construction d'une geofence
    private fun buildGeofence(location: WorkLocationEntity): Geofence {
        return Geofence.Builder()
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
    }

    // 📦 Enregistrement de toutes les geofences actives
    fun registerGeofences(locations: List<WorkLocationEntity>) {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            android.util.Log.e("GEOFENCE", "Permission FINE_LOCATION absente")
            return
        }

        val geofences = locations
            .filter { it.isActive }
            .map { buildGeofence(it) }

        if (geofences.isEmpty()) {
            android.util.Log.w("GEOFENCE", "Aucune geofence active")
            return
        }

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()

        // 🔁 nettoyage avant ajout
        geofencingClient.removeGeofences(geofencePendingIntent)
            .addOnCompleteListener {

                geofencingClient.addGeofences(request, geofencePendingIntent)
                    .addOnSuccessListener {
                        android.util.Log.d(
                            "GEOFENCE",
                            "Geofences enregistrées : ${geofences.size}"
                        )
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e(
                            "GEOFENCE",
                            "Erreur ajout geofence",
                            e
                        )
                    }
            }
    }

    // ❌ Suppression complète (rebuild propre)
    fun clearGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)
    }
}
