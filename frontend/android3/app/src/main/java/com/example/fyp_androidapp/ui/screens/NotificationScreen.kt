package com.example.fyp_androidapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.data.models.Notification
import com.example.fyp_androidapp.ui.components.EventPopupDialog
import com.example.fyp_androidapp.ui.components.NotificationCard
import com.example.fyp_androidapp.viewmodel.NotificationsViewModel
import kotlinx.coroutines.flow.debounce
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: NotificationsViewModel = viewModel()){
    val notifications by viewModel.notifications.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState() // ✅ Observe event details from ViewModel
    LaunchedEffect(calendarEvents) {
        Log.d("NotificationScreen", "Updated Calendar Events: $calendarEvents")
    }
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedNotification by remember { mutableStateOf<Notification?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    // ✅ Log notifications and calendar events before passing them
    LaunchedEffect(notifications) {
        notifications.forEach { notification ->
            val eventDetails = calendarEvents[notification.id]
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                val eventDetails = calendarEvents[notification.id]

                Column {
                    NotificationCard(
                        notification = notification,
                        eventDetails = eventDetails,  // ✅ Pass correct event details
                        onAdd = {
                            selectedNotification = notification
                            showDialog = true
                        },
                        onDiscard = {
                            val eventDetails = calendarEvents[notification.id] ?: EventDetails()
                            viewModel.discardEvent(notification.id, eventDetails)
                        },
                        onLongPress = {  // ✅ Trigger dialog on long press
                            selectedNotification = notification
                            showDialog = true
                        }
                    )
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }
            }

            item {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
    // ✅ Show popup when a notification is selected
    if (showDialog && selectedNotification != null) {
        EventPopupDialog(
            eventDetails = calendarEvents[selectedNotification!!.id] ?: EventDetails(),
            onSave = { newEventDetails ->
                viewModel.addEvent(
                    selectedNotification!!.id,
                    newEventDetails
                ) // ✅ Pass event details for instant update
                showDialog = false  // ✅ Close dialog
            },
            onDismiss = { showDialog = false },
            onDiscard = { notificationId ->
                val eventDetails = calendarEvents[notificationId] ?: EventDetails()
                viewModel.discardEvent(notificationId, eventDetails)
                showDialog = false
            }
        )
    }
}
