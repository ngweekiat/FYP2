package com.example.fyp_androidapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fyp_androidapp.data.models.Notification
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

    // Handle "Add" button press
    fun onAdd(notification: Notification) {
        notifications = notifications.map {
            if (it == notification) it.copy(status = "Aug 11, 3PM") else it
        }
    }

    // Handle "Discard" button press
    fun onDiscard(notification: Notification) {
        notifications = notifications.map {
            if (it == notification) it.copy(status = "Event Discarded") else it
        }
    }

    // Display the notifications
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notifications) { notification ->
            NotificationCard(
                notification = notification,
                onAdd = { onAdd(notification) },
                onDiscard = { onDiscard(notification) }
            )
        }
    }
}
