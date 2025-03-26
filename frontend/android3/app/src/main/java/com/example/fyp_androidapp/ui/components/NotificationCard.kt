package com.example.fyp_androidapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.data.models.Notification
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import com.example.fyp_androidapp.utils.DateUtils
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput


@Composable
fun NotificationCard(
    notification: Notification,
    eventDetails: EventDetails?,
    onAdd: () -> Unit,
    onDiscard: () -> Unit,
    onLongPress: () -> Unit
) {
    var localButtonStatus by remember { mutableStateOf(eventDetails?.buttonStatus) }
    var isExpanded by remember { mutableStateOf(false) } // ✅ State to track expansion

    LaunchedEffect(eventDetails?.buttonStatus) {
        localButtonStatus = eventDetails?.buttonStatus
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
            .pointerInput(Unit) {  // ✅ Detect long press
                detectTapGestures(
                    onLongPress = {
                        Log.d("NotificationCard", "Long Press Detected for ID: ${notification.id}")
                        onLongPress()
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

            if (notification.title.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (notification.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notification.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3, // ✅ Toggle max lines
                    modifier = Modifier.clickable { isExpanded = !isExpanded } // ✅ Click to expand/collapse
                )
            }

            if (notification.isImportant) {
                if (localButtonStatus == null || localButtonStatus == 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { onAdd() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Add", color = MaterialTheme.colorScheme.onPrimary)
                        }
                        OutlinedButton(
                            onClick = {
                                localButtonStatus = 2
                                onDiscard()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Discard", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (localButtonStatus == 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = eventDetails?.title ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = eventDetails?.let {
                                    val formattedDate = if (it.startDate.isNotBlank()) DateUtils.formatDateOnly(it.startDate) else ""
                                    val formattedTime = if (it.startTime.isNotBlank()) DateUtils.formatTimeOnly(it.startTime) else ""
                                    listOf(formattedDate, formattedTime)
                                        .filter { it.isNotEmpty() }
                                        .joinToString(" ")
                                } ?: "No Date",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            // ✅ New: Show location if it exists
                            if (!eventDetails?.location.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "📍 ${eventDetails?.location}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                } else if (localButtonStatus == 2) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Event Discarded",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
