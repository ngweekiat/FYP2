package com.example.fyp_androidapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.data.models.Notification
import com.example.fyp_androidapp.ui.components.EventPopupDialog
import com.example.fyp_androidapp.ui.components.NotificationCard

@Composable
fun NotificationsScreen() {
    // State for notifications list
    var notifications by remember {
        mutableStateOf(
            listOf(
                Notification(
                    sender = "Jonathan",
                    title = "SC3020-Advanced Databases",
                    content = "Perfect! Let's schedule our next meeting for 3PM next Thu.",
                    time = "2m",
                    isImportant = true
                ),
                Notification(
                    sender = "Nicole",
                    title = "Lunch with Nicole",
                    content = "See you next Thursday for lunch!\nAug 30, 12PM-1PM",
                    time = "8h",
                    isImportant = true
                ),
                Notification(
                    sender = "do-not-reply@blackboard.com",
                    title = "S4S2 SC4015-Cyber Security",
                    content = "Classes will be held virtually next Thursday in lieu of public holiday.",
                    time = "14h",
                    isImportant = true
                ),
                Notification(
                    sender = "Financial Times",
                    title = "#CES203 Seminar 2024",
                    content = "We're just days away from #CES203. Financial experts will join...\nJan 15, 9AM-12PM SGT",
                    time = "8h",
                    isImportant = true
                )
            )
        )
    }

    // State to track dialog visibility and the selected notification
    var selectedNotification by remember { mutableStateOf<Notification?>(null) }

    // Handle "Add" button press
    fun onAdd(notification: Notification) {
        selectedNotification = notification // Open the dialog with the selected notification
    }

    // Handle "Discard" button press
    fun onDiscard(notification: Notification) {
        notifications = notifications.map {
            if (it == notification) it.copy(status = "Event Discarded")
            else it
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                Column {
                    NotificationCard(
                        notification = notification,
                        onAdd = { onAdd(notification) },  // Open dialog
                        onDiscard = { onDiscard(notification) }
                    )
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }
            }
        }

        // Display the EventPopupDialog if a notification is selected
        selectedNotification?.let { notification ->
            EventPopupDialog(
                eventDetails = EventDetails(
                    title = notification.title,
                    location = notification.content.substringBefore("\n"), // Extract location or main content
                    startDate = "Aug 30, 2023", // Example static date
                    startTime = "12:00 PM",    // Example static time
                    endDate = "Aug 30, 2023",  // Example static end date
                    endTime = "1:00 PM"        // Example static end time
                ),
                onSave = { eventDetails ->
                    // Save the event and close the dialog
                    notifications = notifications.map {
                        if (it == notification) it.copy(status = "Event Added: ${eventDetails.startDate} ${eventDetails.startTime}")
                        else it
                    }
                    selectedNotification = null
                },
                onDismiss = {
                    // Close the dialog without saving
                    selectedNotification = null
                }
            )
        }
    }
}
