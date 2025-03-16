package com.example.fyp_androidapp.data.repository

import android.util.Log
import com.example.fyp_androidapp.Constants
import com.example.fyp_androidapp.data.models.EventDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class CalendarRepository {
    private val client = OkHttpClient()

    /**
     * Fetch events for a specific month and return a Map of LocalDate to List<EventDetails>.
     */
    suspend fun getEventsForMonth(year: Int, month: Int): Map<LocalDate, List<EventDetails>> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "${Constants.BASE_URL}/notifications/calendar_events?year=$year&month=$month"
                val request = Request.Builder().url(url).get().build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("CalendarRepository", "Failed to fetch events for $year-$month")
                    return@withContext emptyMap()
                }

                val responseBody = response.body?.string() ?: return@withContext emptyMap()
                val jsonArray = JSONArray(responseBody)

                val fetchedEvents = mutableMapOf<LocalDate, MutableList<EventDetails>>()

                for (i in 0 until jsonArray.length()) {
                    val jsonEvent = jsonArray.getJSONObject(i)
                    val eventDate = LocalDate.parse(jsonEvent.getString("start_date"))

                    val eventDetails = EventDetails(
                        id = jsonEvent.optString("id", ""), // ✅ Ensure ID is fetched
                        title = jsonEvent.optString("title", "No Title"),
                        description = jsonEvent.optString("description", "No Description"),
                        locationOrMeeting = jsonEvent.optString("location", "Unknown Location"),
                        allDay = jsonEvent.optBoolean("allDay", false),
                        startDate = jsonEvent.optString("start_date", "Unknown Date"),
                        startTime = jsonEvent.optString("start_time", "Unknown Time"),
                        endDate = jsonEvent.optString("end_date", "Unknown Date"),
                        endTime = jsonEvent.optString("end_time", "Unknown Time"),
                        buttonStatus = jsonEvent.optInt("button_status", 0)
                    )

                    fetchedEvents.getOrPut(eventDate) { mutableListOf() }.add(eventDetails)
                }

                Log.d("CalendarRepository", "Fetched Events: $fetchedEvents") // ✅ Debugging log
                fetchedEvents
            } catch (e: Exception) {
                Log.e("CalendarRepository", "Error fetching events: ${e.message}")
                emptyMap()
            }
        }
    }
}
