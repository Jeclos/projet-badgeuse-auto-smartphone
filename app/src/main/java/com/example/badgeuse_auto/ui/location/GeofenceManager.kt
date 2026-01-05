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
import androidx.annotation.RequiresPermission

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
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    /* ---------------------------------------------------
       🔧 BUILD
       --------------------------------------------------- */

    private fun buildGeofence(location: WorkLocationEntity): Geofence =
        Geofence.Builder()
            .setRequestId(location.geofenceUid)
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

    private fun hasPermissions(): Boolean {
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

        return fineGranted && backgroundGranted
    }

    /* ---------------------------------------------------
       ➕ AJOUT / UPDATE D’UN LIEU
       --------------------------------------------------- */
    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ]
    )
    fun addOrUpdateLocation(location: WorkLocationEntity) {

        if (!hasPermissions()) {
            Log.e("GEOFENCE", "❌ Permissions insuffisantes")
            return
        }

        if (!location.isActive) {
            removeLocation(location)
            return
        }

        val geofence = buildGeofence(location)

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        // 🔁 on supprime d’abord celle du même ID
        geofencingClient
            .removeGeofences(listOf(location.geofenceUid))
            .addOnCompleteListener {

                geofencingClient
                    .addGeofences(request, geofencePendingIntent)
                    .addOnSuccessListener {
                        Log.e(
                            "GEOFENCE",
                            "✅ Geofence ajoutée / MAJ : ${location.name}"
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.e(
                            "GEOFENCE",
                            "❌ Erreur ajout geofence ${location.name}",
                            e
                        )
                    }
            }
    }

    /* ---------------------------------------------------
       ❌ SUPPRESSION D’UN LIEU
       --------------------------------------------------- */

    fun removeLocation(location: WorkLocationEntity) {

        geofencingClient
            .removeGeofences(listOf(location.geofenceUid))
            .addOnSuccessListener {
                Log.e(
                    "GEOFENCE",
                    "🗑️ Geofence supprimée : ${location.name}"
                )
            }
            .addOnFailureListener { e ->
                Log.e(
                    "GEOFENCE",
                    "❌ Erreur suppression geofence ${location.name}",
                    e
                )
            }
    }

    /* ---------------------------------------------------
       🔄 REBUILD GLOBAL (SAFE MODE)
       --------------------------------------------------- */
    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ]
    )
    fun rebuildAll(locations: List<WorkLocationEntity>) {

        if (!hasPermissions()) {
            Log.e("GEOFENCE", "❌ Permissions insuffisantes")
            return
        }

        val geofences = locations
            .filter { it.isActive }
            .map { buildGeofence(it) }

        geofencingClient
            .removeGeofences(geofencePendingIntent)
            .addOnCompleteListener {

                if (geofences.isEmpty()) {
                    Log.w("GEOFENCE", "⚠ Aucun geofence actif")
                    return@addOnCompleteListener
                }

                val request = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofences(geofences)
                    .build()

                geofencingClient
                    .addGeofences(request, geofencePendingIntent)
                    .addOnSuccessListener {
                        Log.e(
                            "GEOFENCE",
                            "✅ Rebuild complet (${geofences.size})"
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.e("GEOFENCE", "❌ Erreur rebuild", e)
                    }
            }
    }
}
