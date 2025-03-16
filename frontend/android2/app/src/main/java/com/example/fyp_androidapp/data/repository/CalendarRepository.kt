package com.example.fyp_androidapp.data.repository

import com.example.fyp_androidapp.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class CalendarRepository {
    private val client = OkHttpClient()

    suspend fun getEventsForMonth(year: Int, month: Int): Map<LocalDate, List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "${Constants.BASE_URL}/notifications/calendar_events?year=$year&month=$month"
                val request = Request.Builder().url(url).get().build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) return@withContext emptyMap()

                val responseBody = response.body?.string() ?: return@withContext emptyMap()
                val jsonArray = JSONArray(responseBody)

                val fetchedEvents = mutableMapOf<LocalDate, List<String>>()

                for (i in 0 until jsonArray.length()) {
                    val jsonEvent = jsonArray.getJSONObject(i)
                    val eventDate = LocalDate.parse(jsonEvent.getString("start_date"))
                    val eventTime = jsonEvent.getString("start_time")
                    val eventTitle = jsonEvent.getString("title")

                    fetchedEvents[eventDate] = fetchedEvents.getOrDefault(eventDate, emptyList()) + "$eventTime - $eventTitle"
                }

                fetchedEvents
            } catch (e: Exception) {
                e.printStackTrace()
                emptyMap()
            }
        }
    }
}
