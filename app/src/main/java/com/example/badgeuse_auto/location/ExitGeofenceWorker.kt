package com.example.badgeuse_auto.location

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.badgeuse_auto.data.PresenceDatabase
import com.example.badgeuse_auto.data.PresenceRepository
import com.example.badgeuse_auto.data.SettingsRepository

class ExitGeofenceWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val geofenceUid =
            inputData.getString("GEOFENCE_UID")
                ?: return Result.success()

        Log.e("EXIT_WORKER", "👷 Worker EXIT uid=$geofenceUid")

        val db = PresenceDatabase.getDatabase(applicationContext)

        val repo = PresenceRepository(
            presenceDao = db.presenceDao(),
            workLocationDao = db.workLocationDao(),
            settingsDao = db.settingsDao()
        )

        val settingsRepo = SettingsRepository(db.settingsDao())

        val workLocation =
            db.workLocationDao().getByGeofenceUid(geofenceUid)
                ?: return Result.success()

        val currentPresence = repo.getCurrentPresence()
            ?: return Result.success()

        val settings = settingsRepo.getSettings()

        /* =======================
           Vérification GPS
           ======================= */

        val radiusMeters = when {
            settings.enterDistance <= 0 -> 300f
            settings.enterDistance > 5_000 -> 5_000f
            else -> settings.enterDistance.toFloat()
        }

        val stillOutside =
            GpsVerifier.isOutside(
                context = applicationContext,
                lat = workLocation.latitude,
                lon = workLocation.longitude,
                radiusMeters = radiusMeters
            )

        if (!stillOutside) {
            Log.e("EXIT_WORKER", "↩️ Re-entrée détectée → sortie annulée")
            return Result.success()
        }

        /* =======================
           Sécurité : entrée entre-temps
           ======================= */

        if (currentPresence.isPending) {
            Log.e("EXIT_WORKER", "⛔ Sortie annulée : entrée en cours")
            return Result.success()
        }

        /* =======================
           CONFIRMATION SORTIE
           ======================= */

        val msg = repo.autoEvent(
            isEnter = false,
            workLocation = workLocation
        )

        Log.e("EXIT_WORKER", "✅ SORTIE CONFIRMÉE → $msg")

        return Result.success()
    }
}
