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
        val triggering = event.triggeringGeofences

        Log.e(
            "GEOFENCE",
            "➡ Transition=$transition | IDs=${triggering?.map { it.requestId }}"
        )

        if (
            transition != Geofence.GEOFENCE_TRANSITION_ENTER &&
            transition != Geofence.GEOFENCE_TRANSITION_EXIT
        ) {
            Log.w("GEOFENCE", "⚠ Transition ignorée")
            return
        }

        val isEntering =
            transition == Geofence.GEOFENCE_TRANSITION_ENTER

        val geofence = triggering?.firstOrNull()
        if (geofence == null) {
            Log.e("GEOFENCE", "❌ Aucun geofence déclenché")
            return
        }

        val geofenceUid = geofence.requestId

        val db = PresenceDatabase.getDatabase(context)
        val repo = PresenceRepository(
            presenceDao = db.presenceDao(),
            workLocationDao = db.workLocationDao(),
            settingsDao = db.settingsDao()
        )

        CoroutineScope(Dispatchers.IO).launch {

            val workLocation =
                db.workLocationDao().getByGeofenceUid(geofenceUid)

            if (workLocation == null) {
                Log.e(
                    "GEOFENCE",
                    "❌ Aucun WorkLocation pour uid=$geofenceUid"
                )
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

            Log.e("GEOFENCE", "✅ autoEvent → $msg")

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
