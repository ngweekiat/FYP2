package com.example.fyp_androidapp.data.repository

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

                    EventDetails(
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
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun discardEvent(eventId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jsonBody = JSONObject().apply {
                    put("button_status", 2)
                }.toString()

                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/notifications/calendar_events/$eventId")
                    .patch(RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody))
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                false
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
                    put("button_status", 1) // Mark as saved
                }

                val request = Request.Builder()
                    .url("${Constants.BASE_URL}/notifications/calendar_events/$notificationId")
                    .patch(RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody.toString()))
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
}
