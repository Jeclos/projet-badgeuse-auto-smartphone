package com.example.badgeuse_auto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.badgeuse_auto.data.PresenceDatabase
import com.example.badgeuse_auto.data.PresenceRepository
import com.example.badgeuse_auto.data.PresenceViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.badgeuse_auto.data.SettingsDatabase


class WorkGeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = com.google.android.gms.location.GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) return

        val transition = geofencingEvent.geofenceTransition
        val type = when (transition) {
            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER -> "ENTREE"
            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT -> "SORTIE"
            else -> null
        } ?: return

        // ----------------------------
        // Charger les deux databases
        // ----------------------------
        val db = PresenceDatabase.getDatabase(context)
        val settingsDb = SettingsDatabase.getDatabase(context)

        // ----------------------------
        // Créer Repository à 4 paramètres
        // ----------------------------
        val repo = PresenceRepository(
            presenceDao = db.presenceDao(),
            workLocationDao = db.workLocationDao(),
            dailySummaryDao = db.dailySummaryDao(),
            settingsDao = settingsDb.settingsDao()   // ← ✔ AJOUT CRITIQUE
        )

        val viewModel = PresenceViewModel(repo)

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.autoEvent(type) { _, message ->
                Toast.makeText(context, message ?: "", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
