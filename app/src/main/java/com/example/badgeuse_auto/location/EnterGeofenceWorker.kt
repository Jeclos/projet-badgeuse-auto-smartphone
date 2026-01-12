package com.example.badgeuse_auto.location

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.badgeuse_auto.data.PresenceDatabase
import com.example.badgeuse_auto.data.PresenceRepository


class EnterGeofenceWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val geofenceUid = inputData.getString("GEOFENCE_UID") ?: return Result.success()
        val token = inputData.getLong("TOKEN", -1L)

        val db = PresenceDatabase.getDatabase(applicationContext)
        val repo = PresenceRepository(
            db.presenceDao(),
            db.workLocationDao(),
            db.settingsDao()
        )

        val storedToken = repo.getPendingEnter(geofenceUid)

        if (storedToken == null || storedToken != token) {
            Log.e("ENTER_WORKER", "⛔ ENTER annulé (token invalide)")
            return Result.success()
        }

        Log.e("ENTER_WORKER", "✅ ENTER validé")

        // Nettoyage token
        repo.clearPendingEnter(geofenceUid)

        val workLocation =
            db.workLocationDao().getByGeofenceUid(geofenceUid)
                ?: return Result.success()

        repo.autoEvent(
            isEnter = true,
            workLocation = workLocation
        )

        return Result.success()
    }

}
