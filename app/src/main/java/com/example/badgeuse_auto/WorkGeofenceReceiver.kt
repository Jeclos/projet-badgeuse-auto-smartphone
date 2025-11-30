package com.example.badgeuse_auto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.badgeuse_auto.data.PresenceDatabase
import com.example.badgeuse_auto.data.PresenceEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkGeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = com.google.android.gms.location.GeofencingEvent.fromIntent(intent)
            ?: return

        if (event.hasError()) return

        val transition = event.geofenceTransition
        val now = System.currentTimeMillis()

        val db = PresenceDatabase.getDatabase(context)
        val dao = db.presenceDao()

        CoroutineScope(Dispatchers.IO).launch {
            when (transition) {
                com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    dao.insert( PresenceEntry(
                        timestamp = now,
                        type = "ENTREE",
                        locationName = "WORK"
                    ))
                }
                com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    dao.insert(    PresenceEntry(
                        timestamp = now,
                        type = "SORTIE",
                        locationName = "WORK"
                    ))
                }
            }
        }
    }
}
