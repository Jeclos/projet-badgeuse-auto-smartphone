package com.example.badgeuse_auto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.badgeuse_auto.data.PresenceDatabase
import com.example.badgeuse_auto.data.PresenceRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.*

class WorkGeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // 🔥 TRACE ABSOLUE
        Log.e("GEOFENCE", "🔥 WorkGeofenceReceiver déclenché")

        val event = GeofencingEvent.fromIntent(intent)

        if (event == null) {
            Log.e("GEOFENCE", "❌ GeofencingEvent = null")
            return
        }

        if (event.hasError()) {
            Log.e(
                "GEOFENCE",
                "❌ Erreur Geofence code=${event.errorCode}"
            )
            return
        }

        val transition = event.geofenceTransition
        val ids = event.triggeringGeofences?.map { it.requestId }

        Log.e(
            "GEOFENCE",
            "➡ Transition=$transition | IDs=$ids"
        )

        // On ne traite que ENTER / EXIT
        if (
            transition != Geofence.GEOFENCE_TRANSITION_ENTER &&
            transition != Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            Log.w("GEOFENCE", "⚠ Transition ignorée")
            return
        }

        val geofence = event.triggeringGeofences?.firstOrNull()
        if (geofence == null) {
            Log.e("GEOFENCE", "❌ Aucun geofence déclenché")
            return
        }

        val workLocationId = geofence.requestId.toLongOrNull()
        if (workLocationId == null) {
            Log.e("GEOFENCE", "❌ requestId invalide")
            return
        }

        val isEntering =
            transition == Geofence.GEOFENCE_TRANSITION_ENTER

        Log.e(
            "GEOFENCE",
            if (isEntering) "📍 ENTER détecté" else "🚪 EXIT détecté"
        )

        val db = PresenceDatabase.getDatabase(context)

        val repo = PresenceRepository(
            presenceDao = db.presenceDao(),
            workLocationDao = db.workLocationDao(),
            settingsDao = db.settingsDao()
        )

        // ⚠ BroadcastReceiver = thread court → IO explicite
        CoroutineScope(Dispatchers.IO).launch {

            val workLocation =
                db.workLocationDao().getById(workLocationId)

            if (workLocation == null) {
                Log.e("GEOFENCE", "❌ WorkLocation introuvable")
                return@launch
            }

            Log.e(
                "GEOFENCE",
                "🏢 Lieu=${workLocation.name}"
            )

            val msg = repo.autoEvent(
                isEnter = isEntering,
                workLocation = workLocation
            )

            Log.e("GEOFENCE", "✅ autoEvent exécuté → $msg")

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    msg,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
