package com.example.fyp_androidapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.data.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class CalendarViewModel(
    private val calendarRepository: CalendarRepository = CalendarRepository()
) : ViewModel() {

    private val _events = MutableStateFlow<Map<LocalDate, List<String>>>(emptyMap())
    val events: StateFlow<Map<LocalDate, List<String>>> = _events

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
}
