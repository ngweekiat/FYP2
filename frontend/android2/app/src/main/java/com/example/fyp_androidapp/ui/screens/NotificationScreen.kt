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
import okhttp3.MediaType.Companion.toMediaTypeOrNull


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

    // Function to fetch event details from backend
    var eventDetailsMap by remember { mutableStateOf(mapOf<String, EventDetails>()) }
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
                    val json = JSONObject(responseBody).optJSONObject("event") ?: return@launch

                    val newEventDetails = EventDetails(
                        title = json.optString("title", "No Title"),
                        description = json.optString("description", "No Description"),
                        locationOrMeeting = json.optString("location", "Unknown Location"),
                        allDay = json.optBoolean("allDay", false),
                        startDate = json.optString("start_date", "Unknown Date"),
                        startTime = json.optString("start_time", "Unknown Time"),
                        endDate = json.optString("end_date", "Unknown Date"),
                        endTime = json.optString("end_time", "Unknown Time"),
                        buttonStatus = json.optInt("button_status", 0)
                    )

                    withContext(Dispatchers.Main) {
                        eventDetailsMap = eventDetailsMap + (notificationId to newEventDetails)

                        // Update notifications list with correct button_status
                        notifications = notifications.map {
                            if (it.id == notificationId) it.copy(button_status = newEventDetails.buttonStatus)
                            else it
                        }

                        eventDetails = newEventDetails // Trigger UI re-render
                    }

                } else {
                    withContext(Dispatchers.Main) {
                        eventDetails = null
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    eventDetails = null
                }
            }
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
                        val notification = Notification(
                            id = item.optString("id", ""),
                            sender = item.optString("appName", "Unknown"),
                            title = item.optString("title", "No Title"),
                            content = item.optString("bigText", item.optString("text", "No Content")),
                            time = formatTimestampToSGT(item.optString("timestamp", "Unknown Time")),
                            isImportant = item.optInt("notification_importance", 0) == 1,
                            button_status = item.optInt("button_status", 0)
                        )
                        newNotifications.add(notification)
                    }

                    val lastVisibleId = json.optString("lastVisible", null)

                    withContext(Dispatchers.Main) {
                        notifications = notifications + newNotifications
                        lastVisible = lastVisibleId

                        // Fetch event details only for important notifications
                        newNotifications.filter { it.isImportant }.forEach { notification ->
                            fetchCalendarEvent(notification.id)
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
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
            if (it.buttonStatus == 2) {
                "Event Discarded"
            } else {
                val formattedDate = formatDate(it.startDate, it.startTime)
                "${it.title}\n$formattedDate"
            }
        }
    }

    fun discardEvent(eventId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val jsonBody = JSONObject().apply {
                    put("button_status", 2)
                }.toString()

                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/notifications/calendar_events/$eventId")
                    .patch(okhttp3.RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    throw Exception("Failed to discard event")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e) }
            }
        }
    }

    fun updateEvent(notificationId: String, updatedEvent: EventDetails) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val jsonBody = JSONObject().apply {
                    put("title", updatedEvent.title)
                    put("description", updatedEvent.description)
                    put("location", updatedEvent.locationOrMeeting)
                    put("allDay", updatedEvent.allDay)
                    put("start_date", updatedEvent.startDate)
                    put("start_time", updatedEvent.startTime)
                    put("end_date", updatedEvent.endDate)
                    put("end_time", updatedEvent.endTime)
                    put("button_status", 1) // Mark as saved
                }

                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/notifications/calendar_events/$notificationId")
                    .patch(okhttp3.RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody.toString()))
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw Exception("Failed to update event: ${response.message}")
                }
            } catch (e: Exception) {
                // Handle error
            }
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
                var eventDetails = eventDetailsMap[notification.id]
                val dynamicStatusMessage = eventDetails?.let { formatEventStatus(it) } ?: notification.status_message

                Column {
                    NotificationCard(
                        notification = notification,
                        statusMessage = dynamicStatusMessage,
                        onAdd = {
                            eventDetails = null // Reset to prevent showing old data
                            fetchCalendarEvent(notification.id) // Fetch new event details
                            selectedNotification = notification // Set notification after fetching details
                        },
                        onDiscard = {
                            discardEvent(notification.id,
                                onSuccess = {
                                    notifications = notifications.map {
                                        if (it.id == notification.id) it.copy(status_message = "Event Discarded", button_status = 2)
                                        else it
                                    }
                                    eventDetailsMap = eventDetailsMap - notification.id // Remove discarded event
                                },
                                onError = { e -> println("Error discarding event: ${e.message}") }
                            )
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

        // Show the event popup dialog immediately when a notification is selected
        selectedNotification?.let { notification ->
            val currentEventDetails = eventDetailsMap[notification.id] ?: eventDetails ?: EventDetails()

            EventPopupDialog(
                eventDetails = currentEventDetails, // Ensure it gets updated details
                onSave = { savedEvent ->
                    updateEvent(notification.id, savedEvent) // API call to update event
                    notifications = notifications.map {
                        if (it.id == notification.id) it.copy(
                            status_message = formatEventStatus(savedEvent),
                            button_status = 1
                        )
                        else it
                    }
                    selectedNotification = null
                },
                onDismiss = { selectedNotification = null }
            )
        }

    }

}