package com.example.badgeuse_auto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.badgeuse_auto.data.BadgeMode
import com.example.badgeuse_auto.data.PresenceDatabase
import com.example.badgeuse_auto.data.PresenceRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.*
import androidx.work.*
import com.example.badgeuse_auto.location.EnterGeofenceWorker
import java.util.concurrent.TimeUnit
import com.example.badgeuse_auto.location.ExitGeofenceWorker




class WorkGeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.e("GEOFENCE", "🔥 WorkGeofenceReceiver déclenché")

        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        val transition = event.geofenceTransition
        val geofence = event.triggeringGeofences?.firstOrNull() ?: return
        val geofenceUid = geofence.requestId

        val isEntering = transition == Geofence.GEOFENCE_TRANSITION_ENTER
        val isExiting = transition == Geofence.GEOFENCE_TRANSITION_EXIT

        val db = PresenceDatabase.getDatabase(context)
        val repo = PresenceRepository(
            db.presenceDao(),
            db.workLocationDao(),
            db.settingsDao()
        )

        CoroutineScope(Dispatchers.IO).launch {

            /* =======================
               ENTER
               ======================= */
            if (isEntering) {

                Log.e("GEOFENCE", "⏳ ENTER détecté → temporisation")

                val now = System.currentTimeMillis()

                // Annule toute sortie en cours
                WorkManager.getInstance(context)
                    .cancelAllWorkByTag("EXIT_$geofenceUid")

                // Annule ancien ENTER
                WorkManager.getInstance(context)
                    .cancelAllWorkByTag("ENTER_$geofenceUid")

                // Sauvegarde token ENTER
                repo.savePendingEnter(geofenceUid, now)

                val delaySec = repo.getEnterDelaySec()

                val workRequest =
                    OneTimeWorkRequestBuilder<EnterGeofenceWorker>()
                        .setInitialDelay(delaySec.toLong(), TimeUnit.SECONDS)
                        .setInputData(
                            workDataOf(
                                "GEOFENCE_UID" to geofenceUid,
                                "TOKEN" to now
                            )
                        )
                        .addTag("ENTER_$geofenceUid")
                        .build()

                WorkManager.getInstance(context).enqueue(workRequest)
                return@launch
            }

            /* =======================
               EXIT
               ======================= */

            val workLocation =
                db.workLocationDao().getByGeofenceUid(geofenceUid)
                    ?: return@launch

            if (!workLocation.isActive) return@launch

            Log.e("GEOFENCE", "⏳ EXIT détecté → temporisation (${workLocation.name})")

            // Annule ENTER en attente
            WorkManager.getInstance(context)
                .cancelAllWorkByTag("ENTER_$geofenceUid")


            // Annule ancien EXIT
            WorkManager.getInstance(context)
                .cancelAllWorkByTag("EXIT_$geofenceUid")

            val delaySec = repo.getExitDelaySec()

            val workRequest =
                OneTimeWorkRequestBuilder<ExitGeofenceWorker>()
                    .setInitialDelay(delaySec.toLong(), TimeUnit.SECONDS)
                    .setInputData(
                        workDataOf("GEOFENCE_UID" to geofenceUid)
                    )
                    .addTag("EXIT_$geofenceUid")
                    .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}




