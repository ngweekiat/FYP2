package com.example.fyp_androidapp.data.repository

import android.util.Log
import com.example.fyp_androidapp.Constants
import com.example.fyp_androidapp.data.models.EventDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class GoogleCalendarApiRepository {
    private val client = OkHttpClient()
    private val backendUrl = "${Constants.BASE_URL}/google-calendar"

    /**
     * Sends an event to the backend to be added to Google Calendar for all authenticated users.
     */
    suspend fun addEventToGoogleCalendar(eventDetails: EventDetails): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$backendUrl/add-event"

                val requestBody = JSONObject().apply {
                    put("eventDetails", JSONObject().apply {
                        put("id", eventDetails.id)
                        put("title", eventDetails.title)
                        put("startDate", eventDetails.startDate)
                        put("startTime", eventDetails.startTime)
                        put("endDate", eventDetails.endDate ?: eventDetails.startDate)
                        put("endTime", eventDetails.endTime ?: eventDetails.startTime)
                        put("locationOrMeeting", eventDetails.locationOrMeeting ?: "")
                        put("description", eventDetails.description ?: "")
                    })
                }.toString()

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("GoogleCalendarApiRepo", "Event added successfully for all users")
                    return@withContext true
                } else {
                    Log.e("GoogleCalendarApiRepo", "Failed to add event: ${response.message}")
                    return@withContext false
                }
            } catch (e: Exception) {
                Log.e("GoogleCalendarApiRepo", "Error adding event", e)
                return@withContext false
            }
        }
    }
}
