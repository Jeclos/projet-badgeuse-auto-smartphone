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

        val event = GeofencingEvent.fromIntent(intent)
        if (event == null || event.hasError()) {
            Log.e("GEOFENCE", "Geofence error")
            return
        }

        Log.d("GEOFENCE", "Transition: ${event.geofenceTransition}")

        if (
            event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER &&
            event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_EXIT
        ) return

        val geofence = event.triggeringGeofences?.firstOrNull() ?: return
        val workLocationId = geofence.requestId.toLongOrNull() ?: return

        val isEntering =
            event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER

        val db = PresenceDatabase.getDatabase(context)

        val repo = PresenceRepository(
            presenceDao = db.presenceDao(),
            workLocationDao = db.workLocationDao(),
            settingsDao = db.settingsDao()
        )

        CoroutineScope(Dispatchers.IO).launch {

            val workLocation =
                db.workLocationDao().getById(workLocationId) ?: return@launch

            val msg = repo.autoEvent(
                isEnter = isEntering,
                workLocation = workLocation
            )

            withContext(Dispatchers.Main) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
