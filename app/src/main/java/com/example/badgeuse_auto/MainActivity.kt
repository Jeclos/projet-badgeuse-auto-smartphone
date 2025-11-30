package com.example.badgeuse_auto

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.badgeuse_auto.data.*
import com.example.badgeuse_auto.ui.navigation.RootNav
import com.example.badgeuse_auto.ui.theme.Badgeuse_AutoTheme
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    private val requestCode = 1001
    private lateinit var viewModel: PresenceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permissions
        requestPermissions(locationPermissions, requestCode)

        // Init database + viewmodel
        val db = PresenceDatabase.getDatabase(this)
        val repo = PresenceRepository(
            presenceDao = db.presenceDao(),
            workLocationDao = db.workLocationDao()
        )
        val factory = PresenceViewModelFactory(repo)
        viewModel = factory.create(PresenceViewModel::class.java)

        enableEdgeToEdge()

        // UI
        setContent {
            Badgeuse_AutoTheme {
                RootNav(
                    viewModel = viewModel,
                    onGeofenceUpdate = { updateGeofence() }   // ⬅️ TRÈS IMPORTANT
                )
            }
        }

        // Au démarrage → créer geofence si un lieu existe
        lifecycleScope.launch {
            val loc = viewModel.getWorkLocation()
            if (loc != null) {
                registerWorkGeofence(loc.latitude, loc.longitude)
            }
        }
    }

    // -------------------------------------------------------------------------
    //  Met à jour le geofence quand un lieu est modifié
    // -------------------------------------------------------------------------
    fun updateGeofence() {
        lifecycleScope.launch {
            val loc = viewModel.getWorkLocation()
            if (loc != null) {
                registerWorkGeofence(loc.latitude, loc.longitude)
            } else {
                Log.w("GEOFENCE", "Aucun lieu enregistré")
            }
        }
    }

    // -------------------------------------------------------------------------
    //  Création du geofence pour un lieu donné
    // -------------------------------------------------------------------------
    private suspend fun registerWorkGeofence(latitude: Double, longitude: Double) {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("GEOFENCE", "Permissions manquantes")
            return
        }

        Log.d("GEOFENCE", "📍 Création du geofence : $latitude / $longitude")

        val geofencingClient = LocationServices.getGeofencingClient(this)

        val geofence = Geofence.Builder()
            .setRequestId("WORK_ZONE")
            .setCircularRegion(latitude, longitude, 150f)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(this, WorkGeofenceReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        geofencingClient.addGeofences(request, pendingIntent)
            .addOnSuccessListener {
                Log.d("GEOFENCE", "✔️ Geofence WORK_ZONE enregistré")
            }
            .addOnFailureListener {
                Log.e("GEOFENCE", "❌ Erreur geofence : ${it.message}")
            }
    }
}
