package com.example.fyp_androidapp.viewmodel

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
    private val userId: String, // ✅ REQUIRED to fetch token from Room
    private val calendarRepository: CalendarRepository = CalendarRepository(),
    private val eventsRepository: EventsRepository = EventsRepository(),
    private val googleCalendarApiRepository: GoogleCalendarApiRepository
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
                    val start = LocalDate.parse(event.startDate)
                    val end = LocalDate.parse(event.endDate)
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

    fun addNewEvent(newEvent: EventDetails) {
        viewModelScope.launch {
            val cleanEvent = newEvent.copy(buttonStatus = 1)

            val result = eventsRepository.addEventToCalendar(cleanEvent.id, cleanEvent)
            val googleSuccess = googleCalendarApiRepository.upsertEventToGoogleCalendar(cleanEvent.id, cleanEvent)

            if (result != null && googleSuccess) {
                val eventDate = LocalDate.parse(cleanEvent.startDate)
                fetchEventsForMonth(eventDate.year, eventDate.monthNumber)
                Log.d("CalendarViewModel", "Event successfully added to Room DB and Google Calendar: $result")
            } else {
                if (result == null) Log.e("CalendarViewModel", "Failed to add event to Room DB")
                if (!googleSuccess) Log.e("CalendarViewModel", "Failed to add event to Google Calendar")
            }
        }
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

                updatedEvents[eventDate] = existingEvents.toList()
                _events.value = updatedEvents.toMap()

                // Update locally and push to Google Calendar
                val success = eventsRepository.updateEvent(updatedEvent.id, updatedEvent)
                val googleSuccess = googleCalendarApiRepository.upsertEventToGoogleCalendar(updatedEvent.id, updatedEvent)

                fetchEventsForMonth(eventDate.year, eventDate.monthNumber)

                if (!success || !googleSuccess) {
                    Log.e("CalendarViewModel", "Failed to update event in backend or Google Calendar")
                } else {
                    Log.d("CalendarViewModel", "Successfully updated event: $updatedEvent")
                }
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Error updating event: ${e.message}")
            }
        }
    }

    fun discardEvent(eventId: String, eventDate: String) {
        viewModelScope.launch {
            Log.d("CalendarViewModel", "Discarding event: $eventId")

            val eventLocalDate = try {
                LocalDate.parse(eventDate)
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Invalid event date: $eventDate")
                return@launch
            }

            val updatedEvents = _events.value.toMutableMap()
            val eventList = updatedEvents[eventLocalDate]?.toMutableList() ?: mutableListOf()

            val eventIndex = eventList.indexOfFirst { it.id == eventId }
            if (eventIndex != -1) {
                val discardedEvent = eventList[eventIndex].copy(buttonStatus = 2)
                eventList[eventIndex] = discardedEvent
                updatedEvents[eventLocalDate] = eventList.toList()
                _events.value = updatedEvents.toMap()
            }

            eventsRepository.discardEvent(eventId)

            // 🧨 Now delete from Google Calendar
            val googleSuccess = googleCalendarApiRepository.deleteEventFromGoogleCalendar(eventId)
            if (!googleSuccess) {
                Log.e("CalendarViewModel", "Failed to delete event from Google Calendar")
            }

            fetchEventsForMonth(eventLocalDate.year, eventLocalDate.monthNumber)

            Log.d("CalendarViewModel", "Event discarded: $eventId")
        }
    }
}
