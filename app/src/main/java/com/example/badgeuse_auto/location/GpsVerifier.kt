package com.example.badgeuse_auto.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.badgeuse_auto.data.WorkLocationEntity
import com.google.android.gms.location.LocationServices
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object GpsVerifier {

    /**
     * 🔍 Vérifie si l'utilisateur est TOUJOURS hors du site
     * après temporisation (anti faux EXIT).
     */
    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    suspend fun isStillOutside(
        context: Context,
        workLocation: WorkLocationEntity,
        radiusMeters: Float
    ): Boolean = suspendCoroutine { cont ->

        val client =
            LocationServices.getFusedLocationProviderClient(context)

        @SuppressLint("MissingPermission")
        client.lastLocation
            .addOnSuccessListener { loc ->
                if (loc == null) {
                    Log.e("GEOFENCE", "❌ GPS null → sortie annulée")
                    cont.resume(false)
                    return@addOnSuccessListener
                }

                val results = FloatArray(1)
                Location.distanceBetween(
                    loc.latitude,
                    loc.longitude,
                    workLocation.latitude,
                    workLocation.longitude,
                    results
                )

                val distance = results[0]

                Log.e(
                    "GEOFENCE",
                    "📏 Distance réelle = ${distance.toInt()}m (radius=$radiusMeters)"
                )

                cont.resume(distance > radiusMeters)
            }
            .addOnFailureListener {
                Log.e("GEOFENCE", "❌ Erreur GPS → sortie annulée")
                cont.resume(false)
            }
    }

    /**
     * 🧩 Version bas niveau conservée (optionnelle)
     * utile pour tests ou autres usages.
     */
    suspend fun isOutside(
        context: Context,
        lat: Double,
        lon: Double,
        radiusMeters: Float
    ): Boolean = suspendCoroutine { cont ->

        val client =
            LocationServices.getFusedLocationProviderClient(context)

        @SuppressLint("MissingPermission")
        client.lastLocation
            .addOnSuccessListener { loc ->
                if (loc == null) {
                    cont.resume(true)
                    return@addOnSuccessListener
                }

                val results = FloatArray(1)
                Location.distanceBetween(
                    loc.latitude,
                    loc.longitude,
                    lat,
                    lon,
                    results
                )

                cont.resume(results[0] > radiusMeters)
            }
            .addOnFailureListener {
                cont.resume(true)
            }
    }
}
