package com.example.fyp_androidapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.fyp_androidapp.Constants



@Composable
fun NotificationsScreen() {
    var notifications by remember { mutableStateOf(listOf<Notification>()) }
    var isLoading by remember { mutableStateOf(false) }
    var lastVisible by remember { mutableStateOf<String?>(null) }
    var selectedNotification by remember { mutableStateOf<Notification?>(null) }
    var eventDetails by remember { mutableStateOf<EventDetails?>(null) }

    val backendUrl = "${Constants.BASE_URL}/notifications"
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mma")

    fun formatTimestampToSGT(timestamp: String): String {
        return try {
            val zonedDateTime = ZonedDateTime.parse(timestamp)
                .withZoneSameInstant(ZoneId.of("Asia/Singapore"))
            zonedDateTime.format(timeFormatter).uppercase()
        } catch (e: Exception) {
            "Unknown Time"
        }
    }

    fun fetchNotifications(limit: Int = 20) {
        if (isLoading) return

        isLoading = true
        coroutineScope.launch(Dispatchers.IO) {
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
                                id = item.optString("id", ""),
                                sender = item.optString("appName", "Unknown"),
                                title = item.optString("title", "No Title"),
                                content = item.optString("bigText", item.optString("text", "No Content")),
                                time = formatTimestampToSGT(item.optString("timestamp", "Unknown Time")),
                                isImportant = item.optInt("notification_importance", 0) == 1
                            )
                        )
                    }

                    val lastVisibleId = json.optString("lastVisible", null)

                    withContext(Dispatchers.Main) {
                        notifications = notifications + newNotifications
                        lastVisible = lastVisibleId
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    // Function to fetch event details from backend
    fun fetchCalendarEvent(notificationId: String) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/notifications/calendar_events/$notificationId")
                    .get()
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val json = JSONObject(responseBody).getJSONObject("event")

                    withContext(Dispatchers.Main) {
                        eventDetails = EventDetails(
                            title = json.optString("title", "No Title"),
                            description = json.optString("description", "No Description"),
                            locationOrMeeting = json.optString("location", "Unknown Location"),
                            allDay = json.optBoolean("allDay", false),
                            startDate = json.optString("start_date", "Unknown Date"),
                            startTime = json.optString("start_time", "Unknown Time"),
                            endDate = json.optString("end_date", "Unknown Date"),
                            endTime = json.optString("end_time", "Unknown Time")
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) { eventDetails = null }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { eventDetails = null }
            }
        }
    }

    // Load initial notifications
    LaunchedEffect(Unit) {
        fetchNotifications()
    }

    // Fetch event details when a notification is selected
    LaunchedEffect(selectedNotification) {
        selectedNotification?.let {
            eventDetails = null // Reset the event details before fetching new data
            fetchCalendarEvent(it.id)
        }
    }


    // Infinite scrolling logic
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
            .debounce(300L)
            .collect { visibleItems ->
                val lastIndex = lazyListState.layoutInfo.totalItemsCount - 1
                val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: -1
                if (lastVisibleIndex >= lastIndex && !isLoading && lastVisible != null) {
                    fetchNotifications()
                }
            }
    }

    fun formatDate(date: String, time: String): String {
        return try {
            // Parse the date
            val localDate = LocalDate.parse(date)
            val formattedDate = localDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))

            // Parse the time (if it's not "Unknown Time")
            val formattedTime = if (time != "Unknown Time") {
                val localTime = LocalTime.parse(time)
                localTime.format(DateTimeFormatter.ofPattern("hh:mma")).uppercase() // 12-hour format with AM/PM
            } else {
                "Unknown Time"
            }

            "$formattedDate $formattedTime"
        } catch (e: Exception) {
            "Invalid Date/Time"
        }
    }


    fun formatEventStatus(event: EventDetails?): String? {
        return event?.let {
            val formattedDate = formatDate(it.startDate, it.startTime)
            "${it.title}\n$formattedDate"
        }
    }


// UI
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                val dynamicStatusMessage = if (notification == selectedNotification) {
                    formatEventStatus(eventDetails) ?: notification.status_message
                } else {
                    notification.status_message
                }

                Column {
                    NotificationCard(
                        notification = notification,
                        statusMessage = dynamicStatusMessage,
                        onAdd = { selectedNotification = notification },
                        onDiscard = {
                            notifications = notifications.map {
                                if (it == notification) it.copy(status_message = "Event Discarded") else it
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

            item {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Display EventPopupDialog with fetched details
        selectedNotification?.let { notification ->
            when {
                eventDetails == null -> {
                    // Show a loading state if event details are being fetched
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
                else -> {
                    EventPopupDialog(
                        eventDetails = eventDetails!!,
                        onSave = { savedEvent ->
                            notifications = notifications.map {
                                if (it == notification) it.copy(status_message = formatEventStatus(savedEvent))
                                else it
                            }
                            selectedNotification = null
                        },
                        onDismiss = { selectedNotification = null }
                    )
                }
            }
        }
    }

}
