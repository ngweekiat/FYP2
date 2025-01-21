package com.example.fyp_androidapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fyp_androidapp.data.models.Notification

@Composable
fun NotificationCard(
    notification: Notification,
    onAdd: () -> Unit,
    onDiscard: () -> Unit
) {
    var statusMessage by remember { mutableStateOf(notification.status) }
    var isActionPerformed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Sender's Name and Time
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = notification.sender,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                if (notification.time.isNotEmpty()) {
                    Text(
                        text = notification.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Title (only displayed if non-empty)
            if (notification.title.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Content (only displayed if non-empty)
            if (notification.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val contentWithoutDate = notification.content.substringBefore("\n")
                Text(
                    text = contentWithoutDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Status or Extracted Date with Bar (only if `statusMessage` is not null or empty)
            statusMessage?.let { status ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusBoxColor =
                        if (status == "Event Discarded") MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary

                    val statusTextColor =
                        if (status == "Event Discarded") MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary

                    // Status Box
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(40.dp)
                            .background(statusBoxColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Status Text
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = statusTextColor
                    )
                }
            }

            // Buttons for Actions
            if (!isActionPerformed && notification.isImportant) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            statusMessage = "Static Extracted Date: 30 Aug, 4PM"
                            isActionPerformed = true
                            onAdd()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    OutlinedButton(
                        onClick = {
                            statusMessage = "Event Discarded"
                            isActionPerformed = true
                            onDiscard()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Discard", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
