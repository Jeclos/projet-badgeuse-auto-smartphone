package com.example.badgeuse_auto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.badgeuse_auto.data.PresenceEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PresenceCard(
    entry: PresenceEntity,
    locationName: String,
    onEdit: (PresenceEntity) -> Unit,
    onDelete: (PresenceEntity) -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(
                    text = "Entrée (${entry.enterType})",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${dateFormatter.format(Date(entry.enterTime))} à ${timeFormatter.format(Date(entry.enterTime))}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Lieu : $locationName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (entry.exitTime != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sortie (${entry.exitType})",
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = timeFormatter.format(Date(entry.exitTime!!)),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "En cours",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row {
                IconButton(onClick = { onEdit(entry) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier")
                }
                IconButton(onClick = { onDelete(entry) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                }
            }
        }
    }
}
