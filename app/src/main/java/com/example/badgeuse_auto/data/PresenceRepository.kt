package com.example.badgeuse_auto.data

import android.util.Log
import com.example.badgeuse_auto.domain.BadgeModeHandler
import com.example.badgeuse_auto.domain.DepotBadgeModeHandler
import com.example.badgeuse_auto.domain.OfficeBadgeModeHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import java.util.Calendar
import com.example.badgeuse_auto.domain.HomeTravelBadgeModeHandler
import com.example.badgeuse_auto.domain.ManualOnlyBadgeModeHandler

class PresenceRepository(
    private val presenceDao: PresenceDao,
    private val workLocationDao: WorkLocationDao,
    private val settingsDao: SettingsDao
) {
    /* =======================
       ENTER TOKEN
       ======================= */

    suspend fun getPendingEnter(uid: String): Long? {
        return settingsDao.getPendingEnter(uid)
    }

    /* ---------------- SETTINGS ---------------- */

    val settings: Flow<SettingsEntity> =
        settingsDao.getSettingsFlow().filterNotNull()

    suspend fun saveSettings(settings: SettingsEntity) {
        settingsDao.insert(settings)
    }

    suspend fun getBadgeMode(): BadgeMode =
        settingsDao.getSettings()?.badgeMode ?: BadgeMode.OFFICE

    suspend fun getEnterDelaySec(): Int {
        return settingsDao.getSettings()?.enterDelaySec ?: 0
    }

    suspend fun getExitDelaySec(): Int {
        return settingsDao.getSettings()?.exitDelaySec ?: 0
    }


    /* ---------------- PRESENCES ---------------- */
    suspend fun savePendingEnter(uid: String, token: Long) {
        settingsDao.updatePendingEnter(uid, token)
    }

    suspend fun clearPendingEnter(uid: String) {
        settingsDao.updatePendingEnter(uid, null)
    }

    suspend fun isPendingEnterValid(uid: String, token: Long): Boolean {
        return settingsDao.getPendingEnter(uid) == token
    }

    suspend fun getCurrentPresence(): PresenceEntity? =
        presenceDao.getCurrentPresence()

    fun getAllPresences(): Flow<List<PresenceEntity>> =
        presenceDao.getAllPresences()

    suspend fun insertPresence(entry: PresenceEntity): Long =
        presenceDao.insert(entry)

    suspend fun updatePresence(entry: PresenceEntity) =
        presenceDao.update(entry)

    suspend fun deletePresence(entry: PresenceEntity) =
        presenceDao.delete(entry)

    /* ---------------- WORK LOCATIONS ---------------- */

    fun getAllWorkLocations(): Flow<List<WorkLocationEntity>> =
        workLocationDao.getAllLocationsFlow()

    suspend fun getAllWorkLocationsOnce(): List<WorkLocationEntity> =
        workLocationDao.getAllLocations()

    fun getActiveWorkLocations(): Flow<List<WorkLocationEntity>> =
        workLocationDao.getActiveLocations()

    suspend fun addWorkLocation(location: WorkLocationEntity): Long =
        workLocationDao.insert(location)

    suspend fun updateWorkLocation(location: WorkLocationEntity) =
        workLocationDao.update(location)

    suspend fun deleteWorkLocation(location: WorkLocationEntity) =
        workLocationDao.delete(location)

    /* ---------------- AUTO GEOFENCE ---------------- */

    suspend fun autoEvent(
        isEnter: Boolean,
        workLocation: WorkLocationEntity
    ): String {

        val now = System.currentTimeMillis()
        val settings = settingsDao.getSettings()
            ?: return "Settings manquants"

        if (
            settings.badgeMode == BadgeMode.HOME_TRAVEL &&
            !workLocation.isActive
        ) {
            return "Lieu non actif – ignoré"
        }

        // 🔒 MODE MANUEL SEUL : TOUT AUTO BLOQUÉ
        if (settings.badgeMode == BadgeMode.MANUAL_ONLY) {
            Log.w("AUTO_EVENT", "Mode MANUAL_ONLY – auto ignoré")
            return "Mode manuel actif – auto désactivé"
        }

        // nettoyage sécurité
        if (settings.badgeMode != BadgeMode.MANUAL_ONLY) {
            presenceDao.closeZombiePresences(
                now - 24 * 60 * 60 * 1000L
            )
        }

        val rawPresence = getCurrentPresence()

        val currentPresence = rawPresence
        var effectivePresence = currentPresence

        Log.e(
            "AUTO_EVENT",
            "mode=${settings.badgeMode} | enter=$isEnter | presence=$currentPresence"
        )

        // 🔒 journée déjà clôturée
        if (currentPresence?.locked == true) {
            return "Journée terminée"
        }

        /* ---------------------------------------------------
           ✅ SORTIE AUTO APRÈS ENTRÉE MANUELLE
           --------------------------------------------------- */
        if (
            !isEnter &&
            currentPresence != null &&
            currentPresence.enterType == "MANUAL"
        ) {
            presenceDao.update(
                currentPresence.copy(
                    exitTime = now,
                    exitType = "AUTO"
                )
            )
            return "Sortie automatique après entrée manuelle"
        }

        // badge manuel prioritaire (ENTRÉE SEULEMENT)
        if (isEnter && currentPresence?.enterType == "MANUAL") {
            return "Badge manuel actif – auto ignoré"
        }

        /* ---------------------------------------------------
           🔒 GESTION FERMETURE MODE DÉPÔT
           --------------------------------------------------- */
        if (
            settings.badgeMode == BadgeMode.DEPOT &&
            currentPresence != null &&
            currentPresence.exitTime == null &&
            !isEnter
        ) {

            val window = computeDepotWindow(
                currentPresence.enterTime,
                settings
            )

            // ❌ AVANT L’HEURE MINI → IGNORÉ
            if (now < window.end) {
                Log.e("DEPOT", "⛔ Sortie avant heure mini ignorée")
                return "Sortie avant heure mini ignorée"
            }

            // ✅ PREMIÈRE SORTIE APRÈS HEURE MINI = FIN DE JOURNÉE
            presenceDao.update(
                currentPresence.copy(
                    exitTime = now,
                    exitType = "AUTO_DEPOT",
                    locked = true
                )
            )

            Log.e("DEPOT", "🔒 Fin de journée dépôt à $now")
            return "Fin de journée dépôt"
        }



        /* ---------------------------------------------------
           🚦 DÉLÉGATION HANDLER
           --------------------------------------------------- */
        val handler: BadgeModeHandler =
            when (settings.badgeMode) {

                BadgeMode.OFFICE ->
                    OfficeBadgeModeHandler(presenceDao)

                BadgeMode.DEPOT ->
                    DepotBadgeModeHandler(presenceDao, settings) // ⚠️ ENTRÉE SEULEMENT

                BadgeMode.HOME_TRAVEL ->
                    HomeTravelBadgeModeHandler(presenceDao, settings)

                BadgeMode.MANUAL_ONLY ->
                    ManualOnlyBadgeModeHandler()
            }


        return when {
            settings.badgeMode == BadgeMode.DEPOT && !isEnter ->
                "Sortie dépôt gérée par règle centrale"

            isEnter ->
                handler.onEnter(now, workLocation, effectivePresence)

            else ->
                handler.onExit(now, workLocation, effectivePresence)
        }


    }
    /* ---------------------------------------------------
       🧠 OUTILS TEMPORELS — CYCLE DÉPÔT
       --------------------------------------------------- */

    data class DepotWindow(
        val start: Long,
        val end: Long
    )

    private fun computeDepotWindow(
        referenceTime: Long,
        settings: SettingsEntity
    ): DepotWindow {

        val refCal = Calendar.getInstance().apply {
            timeInMillis = referenceTime
        }

        val startMinutes =
            settings.depotStartHour * 60 + settings.depotStartMinute
        val endMinutes =
            settings.depotEndHour * 60 + settings.depotEndMinute

        val refMinutes =
            refCal.get(Calendar.HOUR_OF_DAY) * 60 +
                    refCal.get(Calendar.MINUTE)

        val startCal = Calendar.getInstance().apply {
            timeInMillis = referenceTime
            set(Calendar.HOUR_OF_DAY, settings.depotStartHour)
            set(Calendar.MINUTE, settings.depotStartMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCal = Calendar.getInstance().apply {
            timeInMillis = startCal.timeInMillis
            set(Calendar.HOUR_OF_DAY, settings.depotEndHour)
            set(Calendar.MINUTE, settings.depotEndMinute)
        }

        // 🌙 CAS NUIT (22h → 5h)
        if (endMinutes <= startMinutes) {

            if (refMinutes < endMinutes) {
                startCal.add(Calendar.DAY_OF_MONTH, -1)
            }

            endCal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return DepotWindow(
            start = startCal.timeInMillis,
            end = endCal.timeInMillis
        )
    }

}
