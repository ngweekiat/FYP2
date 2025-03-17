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
     * Creates or updates an event in Google Calendar for all authenticated users.
     */
    suspend fun upsertEventToGoogleCalendar(eventId: String, eventDetails: EventDetails): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$backendUrl/upsert-event"

                val requestBody = JSONObject().apply {
                    put("eventId", eventId)
                    put("eventDetails", JSONObject().apply {
                        put("title", eventDetails.title)
                        put("startDate", eventDetails.startDate)
                        put("startTime", eventDetails.startTime)
                        put("endDate", eventDetails.endDate ?: eventDetails.startDate)
                        put("endTime", eventDetails.endTime ?: eventDetails.startTime)
                        put("locationOrMeeting", eventDetails.locationOrMeeting ?: "")
                        put("description", eventDetails.description ?: "")
                    })
                }.toString()

//                Log.d("GoogleCalendarApiRepo", "Sending upsert request: $requestBody")

                val request = Request.Builder()
                    .url(url)
                    .put(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("GoogleCalendarApiRepo", "Event upserted successfully")
                    return@withContext true
                } else {
                    Log.e("GoogleCalendarApiRepo", "Failed to upsert event: ${response.message}")
                    return@withContext false
                }
            } catch (e: Exception) {
                Log.e("GoogleCalendarApiRepo", "Error upserting event", e)
                return@withContext false
            }
        }
    }

    suspend fun deleteEventFromGoogleCalendar(eventId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$backendUrl/delete-event"

                val requestBody = JSONObject().apply {
                    put("eventId", eventId)
                }.toString()

                val request = Request.Builder()
                    .url(url)
                    .delete(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("GoogleCalendarApiRepo", "Event deleted successfully")
                    return@withContext true
                } else {
                    Log.e("GoogleCalendarApiRepo", "Failed to delete event: ${response.message}")
                    return@withContext false
                }
            } catch (e: Exception) {
                Log.e("GoogleCalendarApiRepo", "Error deleting event", e)
                return@withContext false
            }
        }
    }

}
