package com.example.badgeuse_auto.location

import android.content.Context
import androidx.work.*

import java.util.concurrent.TimeUnit

object ExitGeofenceWorkerScheduler {

    private const val WORK_NAME_PREFIX = "EXIT_GEOFENCE_"

    fun schedule(
        context: Context,
        geofenceUid: String,
        delayMs: Long
    ) {
        val workName = WORK_NAME_PREFIX + geofenceUid

        val workRequest =
            OneTimeWorkRequestBuilder<ExitGeofenceWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        "GEOFENCE_UID" to geofenceUid
                    )
                )
                .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    fun cancel(context: Context, geofenceUid: String) {
        val workName = WORK_NAME_PREFIX + geofenceUid
        WorkManager.getInstance(context).cancelUniqueWork(workName)
    }
}
