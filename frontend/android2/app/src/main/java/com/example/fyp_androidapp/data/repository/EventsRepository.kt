package com.example.fyp_androidapp.data.repository

import android.util.Log
import com.example.fyp_androidapp.Constants
import com.example.fyp_androidapp.data.models.EventDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class EventsRepository {

    private val client = OkHttpClient()

    suspend fun fetchCalendarEvent(notificationId: String): EventDetails? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/notifications/calendar_events/$notificationId")
                    .get()
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val json = JSONObject(responseBody).optJSONObject("event") ?: return@withContext null

                    val event = EventDetails(
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

                    Log.d("EventsRepository", "Fetched Event: $event for Notification ID: $notificationId")
                    event
                } else {
                    Log.e("EventsRepository", "Failed to fetch event for Notification ID: $notificationId")
                    null
                }
            } catch (e: Exception) {
                Log.e("EventsRepository", "Error fetching event: ${e.message}")
                null
            }
        }
    }

    suspend fun addEventToCalendar(notificationId: String): EventDetails? {
        return withContext(Dispatchers.IO) {
            try {
                val jsonBody = JSONObject().apply {
                    put("button_status", 1) // ✅ Marks event as added
                }.toString()

                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/notifications/calendar_events/$notificationId")
                    .patch(RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody))
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val json = JSONObject(responseBody).optJSONObject("event") ?: return@withContext null

                    val updatedEvent = EventDetails(
                        title = json.optString("title", "No Title"),
                        description = json.optString("description", "No Description"),
                        locationOrMeeting = json.optString("location", "Unknown Location"),
                        allDay = json.optBoolean("allDay", false),
                        startDate = json.optString("start_date", "Unknown Date"),
                        startTime = json.optString("start_time", "Unknown Time"),
                        endDate = json.optString("end_date", "Unknown Date"),
                        endTime = json.optString("end_time", "Unknown Time"),
                        buttonStatus = 1
                    )

                    Log.d("EventsRepository", "Event Added: $updatedEvent for Notification ID: $notificationId")
                    updatedEvent
                } else {
                    Log.e("EventsRepository", "Failed to add event for Notification ID: $notificationId")
                    null
                }
            } catch (e: Exception) {
                Log.e("EventsRepository", "Error adding event: ${e.message}")
                null
            }
        }
    }

    suspend fun discardEvent(eventId: String): EventDetails? {
        return withContext(Dispatchers.IO) {
            try {
                val jsonBody = JSONObject().apply {
                    put("button_status", 2) // ✅ Marks event as discarded
                }.toString()

                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/notifications/calendar_events/$eventId")
                    .patch(RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody))
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val json = JSONObject(responseBody).optJSONObject("event") ?: return@withContext null

                    val updatedEvent = EventDetails(
                        title = json.optString("title", "No Title"),
                        description = json.optString("description", "No Description"),
                        locationOrMeeting = json.optString("location", "Unknown Location"),
                        allDay = json.optBoolean("allDay", false),
                        startDate = json.optString("start_date", "Unknown Date"),
                        startTime = json.optString("start_time", "Unknown Time"),
                        endDate = json.optString("end_date", "Unknown Date"),
                        endTime = json.optString("end_time", "Unknown Time"),
                        buttonStatus = 2
                    )

                    Log.d("EventsRepository", "Event Discarded: $updatedEvent for Event ID: $eventId")
                    updatedEvent
                } else {
                    Log.e("EventsRepository", "Failed to discard event for Event ID: $eventId")
                    null
                }
            } catch (e: Exception) {
                Log.e("EventsRepository", "Error discarding event: ${e.message}")
                null
            }
        }
    }

    suspend fun updateEvent(notificationId: String, updatedEvent: EventDetails): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jsonBody = JSONObject().apply {
                    put("title", updatedEvent.title)
                    put("description", updatedEvent.description)
                    put("location", updatedEvent.locationOrMeeting)
                    put("allDay", updatedEvent.allDay)
                    put("start_date", updatedEvent.startDate)
                    put("start_time", updatedEvent.startTime)
                    put("end_date", updatedEvent.endDate)
                    put("end_time", updatedEvent.endTime)
                    put("button_status", 1) // ✅ Marks as saved
                }

                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/notifications/calendar_events/$notificationId")
                    .patch(RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody.toString()))
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("EventsRepository", "Event Updated: $updatedEvent for Notification ID: $notificationId")
                    true
                } else {
                    Log.e("EventsRepository", "Failed to update event for Notification ID: $notificationId")
                    false
                }
            } catch (e: Exception) {
                Log.e("EventsRepository", "Error updating event: ${e.message}")
                false
            }
        }
    }
}
