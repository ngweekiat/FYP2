package com.example.fyp_androidapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.data.repository.CalendarRepository
import com.example.fyp_androidapp.data.repository.EventsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class CalendarViewModel(
    private val calendarRepository: CalendarRepository = CalendarRepository(),
    private val eventsRepository: EventsRepository = EventsRepository() // ✅ Now correctly assigned
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
            _events.value = fetchedEvents
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
}
