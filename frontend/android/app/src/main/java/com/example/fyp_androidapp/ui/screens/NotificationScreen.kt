package com.example.fyp_androidapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.data.models.Notification
import com.example.fyp_androidapp.ui.components.EventPopupDialog
import com.example.fyp_androidapp.ui.components.NotificationCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@Composable
fun NotificationsScreen() {
    var notifications by remember { mutableStateOf(listOf<Notification>()) }
    var isLoading by remember { mutableStateOf(false) }
    var lastVisible by remember { mutableStateOf<String?>(null) }
    val backendUrl = "http://192.168.0.124:3000/api/notifications"

    // State to track dialog visibility and the selected notification
    var selectedNotification by remember { mutableStateOf<Notification?>(null) }


    val lazyListState = rememberLazyListState()

    // Function to fetch notifications with pagination
    fun fetchNotifications(limit: Int = 20) {
        if (isLoading) return // Prevent multiple simultaneous fetches

        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val url = StringBuilder("$backendUrl?limit=$limit")
                if (lastVisible != null) url.append("&startAfter=$lastVisible")

                val request = Request.Builder().url(url.toString()).get().build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val json = JSONObject(responseBody)

                    val notificationsArray = json.getJSONArray("notifications")
                    val newNotifications = mutableListOf<Notification>()

                    for (i in 0 until notificationsArray.length()) {
                        val item = notificationsArray.getJSONObject(i)
                        newNotifications.add(
                            Notification(
                                sender = item.optString("appName", "Unknown"),
                                title = item.optString("title", "No Title"),
                                content = item.optString("bigText", item.optString("text", "No Content")),
                                time = item.optString("timestamp", "Unknown Time"),
                                isImportant = false
                            )
                        )
                    }

                    val lastVisibleId = json.optString("lastVisible", null)

                    // Update notifications and the last visible ID
                    notifications = notifications + newNotifications
                    lastVisible = lastVisibleId
                } else {
                    // Log error or show a message
                }
            } catch (e: Exception) {
                // Log error or show a message
            } finally {
                isLoading = false
            }
        }
    }

    // Load initial notifications
    LaunchedEffect(Unit) {
        fetchNotifications()
    }

    // Infinite scrolling logic
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
            .debounce(300L) // Prevent rapid successive fetches
            .collect { visibleItems ->
                val lastIndex = lazyListState.layoutInfo.totalItemsCount - 1
                val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: -1
                if (lastVisibleIndex >= lastIndex && !isLoading && lastVisible != null) {
                    fetchNotifications()
                }
            }
    }

    // UI: LazyColumn with infinite scrolling
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                Column {
                    NotificationCard(
                        notification = notification,
                        onAdd = { selectedNotification = notification }, // Open dialog
                        onDiscard = {
                            notifications = notifications.map {
                                if (it == notification) it.copy(status = "Event Discarded") else it
                            }
                        }
                    )
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }
            }

            // Show a loading indicator at the bottom when fetching
            item {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Display the EventPopupDialog if a notification is selected
        selectedNotification?.let { notification ->
            EventPopupDialog(
                eventDetails = EventDetails(
                    title = notification.title,
                    description = notification.content,
                    locationOrMeeting = notification.content.substringBefore("\n"),
                    allDay = false,
                    startDate = "Aug 30, 2023",
                    startTime = "12:00 PM",
                    endDate = "Aug 30, 2023",
                    endTime = "1:00 PM"
                ),
                onSave = { eventDetails ->
                    notifications = notifications.map {
                        if (it == notification) it.copy(status = "Event Added: ${eventDetails.startDate} ${eventDetails.startTime}")
                        else it
                    }
                    selectedNotification = null
                },
                onDismiss = { selectedNotification = null }
            )
        }
    }
}




//Old code:
//package com.example.fyp_androidapp.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.example.fyp_androidapp.data.models.EventDetails
//import com.example.fyp_androidapp.data.models.Notification
//import com.example.fyp_androidapp.ui.components.EventPopupDialog
//import com.example.fyp_androidapp.ui.components.NotificationCard
//
//@Composable
//fun NotificationsScreen() {
//    // State for notifications list
//    var notifications by remember {
//        mutableStateOf(
//            listOf(
//                Notification(
//                    sender = "Jonathan",
//                    title = "SC3020-Advanced Databases",
//                    content = "Perfect! Let's schedule our next meeting for 3PM next Thu.",
//                    time = "2m",
//                    isImportant = true
//                ),
//                Notification(
//                    sender = "Nicole",
//                    title = "Lunch with Nicole",
//                    content = "See you next Thursday for lunch!\nAug 30, 12PM-1PM",
//                    time = "8h",
//                    isImportant = true
//                ),
//                Notification(
//                    sender = "do-not-reply@blackboard.com",
//                    title = "S4S2 SC4015-Cyber Security",
//                    content = "Classes will be held virtually next Thursday in lieu of public holiday.",
//                    time = "14h",
//                    isImportant = true
//                ),
//                Notification(
//                    sender = "Financial Times",
//                    title = "#CES203 Seminar 2024",
//                    content = "We're just days away from #CES203. Financial experts will join...\nJan 15, 9AM-12PM SGT",
//                    time = "8h",
//                    isImportant = true
//                )
//            )
//        )
//    }
//
//    // State to track dialog visibility and the selected notification
//    var selectedNotification by remember { mutableStateOf<Notification?>(null) }
//
//    // Handle "Add" button press
//    fun onAdd(notification: Notification) {
//        selectedNotification = notification // Open the dialog with the selected notification
//    }
//
//    // Handle "Discard" button press
//    fun onDiscard(notification: Notification) {
//        notifications = notifications.map {
//            if (it == notification) it.copy(status = "Event Discarded")
//            else it
//        }
//    }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 16.dp, vertical = 8.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            items(notifications) { notification ->
//                Column {
//                    NotificationCard(
//                        notification = notification,
//                        onAdd = { onAdd(notification) },  // Open dialog
//                        onDiscard = { onDiscard(notification) }
//                    )
//                    Divider(
//                        modifier = Modifier.padding(vertical = 4.dp),
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
//                        thickness = 1.dp
//                    )
//                }
//            }
//        }
//
//        // Display the EventPopupDialog if a notification is selected
//        selectedNotification?.let { notification ->
//            EventPopupDialog(
//                eventDetails = EventDetails(
//                    title = notification.title,
//                    description = notification.content, // Populate description with the notification content
//                    locationOrMeeting = notification.content.substringBefore("\n"), // Extract location or meeting info if present
//                    allDay = false, // Default to false; user can toggle
//                    startDate = "Aug 30, 2023", // Example static date
//                    startTime = "12:00 PM",    // Example static time
//                    endDate = "Aug 30, 2023",  // Example static end date
//                    endTime = "1:00 PM"        // Example static end time
//                ),
//                onSave = { eventDetails ->
//                    // Save the event and close the dialog
//                    notifications = notifications.map {
//                        if (it == notification) it.copy(status = "Event Added: ${eventDetails.startDate} ${eventDetails.startTime}")
//                        else it
//                    }
//                    selectedNotification = null
//                },
//                onDismiss = {
//                    // Close the dialog without saving
//                    selectedNotification = null
//                }
//            )
//        }
//    }
//}