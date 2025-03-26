package com.example.fyp_androidapp.data.repository

import android.util.Log
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.database.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import java.time.format.DateTimeParseException

class CalendarRepository {

    private val db = DatabaseProvider.getDatabase()
    private val eventDao = db.eventDao()

    /**
     * Fetch events for a specific month and return a Map of LocalDate to List<EventDetails>.
     */
    suspend fun getEventsForMonth(year: Int, month: Int): Map<LocalDate, List<EventDetails>> {
        return withContext(Dispatchers.IO) {
            try {
                val allEvents = eventDao.getAllEvents().filter { it.buttonStatus == 1 }

                Log.d("CalendarRepository", "All Events from Room DB:\n${allEvents.joinToString("\n")}")

                val eventsMap = mutableMapOf<LocalDate, MutableList<EventDetails>>()

                for (event in allEvents) {
                    try {
                        val eventDate = LocalDate.parse(event.startDate)
                        if (eventDate.year == year && eventDate.monthNumber == month) {
                            val eventDetails = EventDetails(
                                id = event.id,
                                title = event.title,
                                description = event.description,
                                location = "", // Add if needed
                                allDay = event.allDay,
                                startDate = event.startDate,
                                startTime = event.startTime,
                                endDate = event.endDate,
                                endTime = event.endTime,
                                buttonStatus = 1
                            )
                            eventsMap.getOrPut(eventDate) { mutableListOf() }.add(eventDetails)
                        }
                    } catch (e: DateTimeParseException) {
                        Log.w("CalendarRepository", "Skipping event with invalid date: ${event.startDate}")
                    }
                }

                Log.d("CalendarRepository", "Filtered Events for $month/$year:\n$eventsMap")
                eventsMap
            } catch (e: Exception) {
                Log.e("CalendarRepository", "Error fetching events: ${e.message}")
                emptyMap()
            }
        }
    }
}