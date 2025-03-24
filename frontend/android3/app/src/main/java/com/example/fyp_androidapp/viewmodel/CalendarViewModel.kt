package com.example.fyp_androidapp.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.data.repository.CalendarRepository
import com.example.fyp_androidapp.data.repository.EventsRepository
import com.example.fyp_androidapp.data.repository.GoogleCalendarApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class CalendarViewModel(
    private val calendarRepository: CalendarRepository = CalendarRepository(),
    private val eventsRepository: EventsRepository = EventsRepository(),
    private val googleCalendarApiRepository: GoogleCalendarApiRepository = GoogleCalendarApiRepository() // ✅ Inject GoogleCalendarApiRepository
) : ViewModel() {

    private val _events = MutableStateFlow<Map<LocalDate, List<EventDetails>>>(emptyMap())
    val events: StateFlow<Map<LocalDate, List<EventDetails>>> = _events

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    private val _isPopupVisible = MutableStateFlow(false)
    val isPopupVisible: StateFlow<Boolean> = _isPopupVisible

    private val _selectedEventDetails = MutableStateFlow(EventDetails())
    val selectedEventDetails: StateFlow<EventDetails> = _selectedEventDetails

    fun fetchEventsForMonth(year: Int, month: Int) {
        viewModelScope.launch {
            val fetchedEvents = calendarRepository.getEventsForMonth(year, month)
            val expandedEvents = mutableMapOf<LocalDate, MutableList<EventDetails>>()

            fetchedEvents.values.flatten().forEach { event ->
                try {
                    val start = kotlinx.datetime.LocalDate.parse(event.startDate)
                    val end = kotlinx.datetime.LocalDate.parse(event.endDate)
                    var current = start

                    while (current <= end) {
                        if (current.year == year && current.monthNumber == month) {
                            expandedEvents.getOrPut(current) { mutableListOf() }.add(event)
                        }
                        current = current.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
                    }
                } catch (e: Exception) {
                    Log.e("CalendarViewModel", "Invalid date in event: ${event.id}")
                }
            }

            _events.value = expandedEvents
        }
    }


    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun showEventPopup(eventDetails: EventDetails) {
        _selectedEventDetails.value = eventDetails
        _isPopupVisible.value = true
    }

    fun hideEventPopup() {
        _isPopupVisible.value = false
    }

    fun updateEvent(updatedEvent: EventDetails) {
        viewModelScope.launch {
            try {
                if (updatedEvent.id.isEmpty()) {
                    Log.e("CalendarViewModel", "Cannot update event: Missing ID")
                    return@launch
                }

                val eventDate = try {
                    LocalDate.parse(updatedEvent.startDate)
                } catch (e: Exception) {
                    Log.e("CalendarViewModel", "Invalid event start date: ${updatedEvent.startDate}")
                    return@launch
                }

                val updatedEvents = _events.value.toMutableMap()

                val existingEvents = updatedEvents[eventDate]?.toMutableList() ?: mutableListOf()
                val index = existingEvents.indexOfFirst { it.id == updatedEvent.id }

                if (index != -1) {
                    existingEvents[index] = updatedEvent
                } else {
                    existingEvents.add(updatedEvent)
                }
                updatedEvents[eventDate] = existingEvents.toList() // ✅ Force recomposition by creating a new list

                _events.value = updatedEvents.toMap() // ✅ Force recomposition by assigning a new instance

                val success = eventsRepository.updateEvent(updatedEvent.id, updatedEvent)

                // Update to google
                googleCalendarApiRepository.upsertEventToGoogleCalendar(updatedEvent.id, updatedEvent)
                fetchEventsForMonth(eventDate.year, eventDate.monthNumber)

                if (!success) {
                    Log.e("CalendarViewModel", "Failed to update event on backend: $updatedEvent")
                } else {
                    Log.d("CalendarViewModel", "Successfully updated event: $updatedEvent")
                    fetchEventsForMonth(eventDate.year, eventDate.monthNumber)
                }
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Error updating event: ${e.message}")
            }
        }
    }

    fun discardEvent(eventId: String, eventDate: String) {
        viewModelScope.launch {
            Log.d("CalendarViewModel", "Discarding event: $eventId")

            // Call backend API to delete event from Google Calendar
//            val success = googleCalendarApiRepository.deleteEventFromGoogleCalendar(eventId)
            Log.d("CalendarViewModel", "Event successfully deleted from Google Calendar")

            // Convert eventDate to LocalDate for map lookup
            val eventLocalDate = try {
                LocalDate.parse(eventDate)
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Invalid event start date: $eventDate")
                return@launch
            }

            // Update local state: Remove the discarded event from _events map
            val updatedEvents = _events.value.toMutableMap()
            val eventList = updatedEvents[eventLocalDate]?.toMutableList() ?: mutableListOf()

            val eventIndex = eventList.indexOfFirst { it.id == eventId }
            if (eventIndex != -1) {
                val discardedEvent = eventList[eventIndex].copy(buttonStatus = 2) // ✅ Mark as discarded
                eventList[eventIndex] = discardedEvent
                updatedEvents[eventLocalDate] = eventList.toList() // ✅ Force recomposition
                _events.value = updatedEvents.toMap()
            }

            // Notify backend to update event status
            eventsRepository.discardEvent(eventId)

            fetchEventsForMonth(eventLocalDate.year, eventLocalDate.monthNumber) // 🔄 Refresh after discard

            Log.d("CalendarViewModel", "Event discarded: $eventId")
        }
    }

}
