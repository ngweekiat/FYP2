package com.example.fyp_androidapp.data.repository

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import com.example.fyp_androidapp.Constants
import com.example.fyp_androidapp.data.models.EventDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventsRepository {

    private val client = OkHttpClient()

    // Function to fetch event details for a given notification ID
    suspend fun fetchEventDetails(notificationId: String): EventDetails? {
        val url = "${Constants.BASE_URL}/notifications/calendar_events/$notificationId"
        val request = Request.Builder().url(url).get().build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return@withContext null
                val json = JSONObject(responseBody).optJSONObject("event") ?: return@withContext null

                return@withContext EventDetails(
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
                return@withContext null // Return null if the request fails
            }
        }
    }

    // Function to update an event (e.g., modifying title, time, etc.)
    suspend fun updateEvent(notificationId: String, updatedEvent: EventDetails): Boolean {
        val url = "${Constants.BASE_URL}/notifications/calendar_events/$notificationId"

        val jsonBody = JSONObject().apply {
            put("title", updatedEvent.title)
            put("description", updatedEvent.description)
            put("location", updatedEvent.locationOrMeeting)
            put("allDay", updatedEvent.allDay)
            put("start_date", updatedEvent.startDate)
            put("start_time", updatedEvent.startTime)
            put("end_date", updatedEvent.endDate)
            put("end_time", updatedEvent.endTime)
            put("button_status", 1) // Mark event as updated
        }.toString()

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody)

        val request = Request.Builder()
            .url(url)
            .patch(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        }
    }

    // Function to discard an event (mark as discarded)
    suspend fun discardEvent(notificationId: String): Boolean {
        val url = "${Constants.BASE_URL}/notifications/calendar_events/$notificationId"

        val jsonBody = JSONObject().apply {
            put("button_status", 2) // Mark event as discarded
        }.toString()

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody)

        val request = Request.Builder()
            .url(url)
            .patch(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        }
    }
}
