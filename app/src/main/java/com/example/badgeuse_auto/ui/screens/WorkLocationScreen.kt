package com.example.badgeuse_auto.ui.screens

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.badgeuse_auto.data.WorkLocationEntity
import com.example.badgeuse_auto.data.PresenceViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton


@SuppressLint("MissingPermission")
@Composable
fun WorkLocationScreen(
    viewModel: PresenceViewModel,
    onBack: () -> Unit,
    onWorkLocationSaved: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }

    // Charger automatiquement le lieu existant au démarrage du composable
    LaunchedEffect(Unit) {
        val saved = viewModel.refreshWorkLocation()
        if (saved != null) {
            name = saved.name
            latitude = saved.latitude.toString()
            longitude = saved.longitude.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            "Définir mon lieu de travail",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom du lieu", color = Color.Black) },
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude", color = Color.Black) },
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude", color = Color.Black) },
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // Utiliser la localisation actuelle
        Button(
            onClick = {
                val client = LocationServices.getFusedLocationProviderClient(context)
                client.lastLocation.addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        latitude = loc.latitude.toString()
                        longitude = loc.longitude.toString()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Utiliser ma localisation actuelle")
        }

        Spacer(Modifier.height(20.dp))

        // ENREGISTRER
        Button(
            onClick = {
                if (name.isNotEmpty() && latitude.isNotEmpty() && longitude.isNotEmpty()) {
                    scope.launch {
                        // appel suspend via coroutine
                        viewModel.saveWorkLocation(
                            WorkLocationEntity(
                                name = name,
                                latitude = latitude.toDouble(),
                                longitude = longitude.toDouble()
                            )
                        )
                        onWorkLocationSaved()
                        onBack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enregistrer")
        }

        Spacer(Modifier.height(10.dp))

        // RETOUR
        OutlinedButton(
            onClick = { onBack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Retour")
        }
    }
}
