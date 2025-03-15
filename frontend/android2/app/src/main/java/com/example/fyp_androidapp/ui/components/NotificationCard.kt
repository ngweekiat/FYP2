package com.example.fyp_androidapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.data.models.Notification

@Composable
fun NotificationCard(
    notification: Notification,
    eventDetails: EventDetails?,
    statusMessage: String?,
    onAdd: () -> Unit,
    onDiscard: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

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

            // Show notification Sender
            if (notification.title.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Show notification Content
            if (notification.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notification.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3, // Expand text when clicked
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )
            }

            // Show event details if event is saved
            if (eventDetails != null && eventDetails.buttonStatus == 1){
                Spacer(modifier = Modifier.height(8.dp))
                EventDetailsCard(eventDetails) // Display event details
            }




        }
    }
}

@Composable
fun EventDetailsCard(eventDetails: EventDetails) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Event Details", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Title: ${eventDetails.title}")
            Text(text = "Date: ${eventDetails.startDate}")
            Text(text = "Time: ${eventDetails.startTime} - ${eventDetails.endTime}")
            Text(text = "Location: ${eventDetails.locationOrMeeting}")
            eventDetails.description.takeIf { it.isNotEmpty() }?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Description: $it")
            }
        }
    }
}