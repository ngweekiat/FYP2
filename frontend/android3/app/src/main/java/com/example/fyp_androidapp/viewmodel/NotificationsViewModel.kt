package com.example.fyp_androidapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.data.models.Notification
import com.example.fyp_androidapp.data.repository.EventsRepository
import com.example.fyp_androidapp.data.repository.GoogleCalendarApiRepository
import com.example.fyp_androidapp.data.repository.NotificationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificationsRepository: NotificationsRepository = NotificationsRepository(),
    private val eventsRepository: EventsRepository = EventsRepository(),
    private val googleCalendarApiRepository: GoogleCalendarApiRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _calendarEvents = MutableStateFlow<Map<String, EventDetails>>(emptyMap())
    val calendarEvents: StateFlow<Map<String, EventDetails>> = _calendarEvents

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        observeNotificationsFlow()
    }

    private fun observeNotificationsFlow() {
        viewModelScope.launch {
            notificationsRepository.notificationsFlow.collectLatest { notificationList ->
                _notifications.value = notificationList

                // Auto-fetch events for important notifications
                notificationList.filter { it.isImportant }.forEach { notification ->
                    fetchCalendarEvent(notification.id)
                }
            }
        }
    }

    fun fetchCalendarEvent(notificationId: String) {
        viewModelScope.launch {
            repeat(5) { attempt ->
                val eventDetails = eventsRepository.fetchCalendarEvent(notificationId)
                if (eventDetails != null) {
                    _calendarEvents.value = _calendarEvents.value + (notificationId to eventDetails)
                    return@launch
                } else {
                    Log.d("NotificationsViewModel", "Attempt $attempt: Event not ready for $notificationId")
                    kotlinx.coroutines.delay(1000) // Wait 1 second and retry
                }
            }
            Log.w("NotificationsViewModel", "Failed to load event after retries: $notificationId")
        }
    }


    fun addEvent(notificationId: String, newEventDetails: EventDetails) {
        viewModelScope.launch {
            Log.d("EventDetails", newEventDetails.toString())

            // Update UI immediately
            val newEvent = newEventDetails.copy(buttonStatus = 1)
            val updatedMap = _calendarEvents.value.toMutableMap()
            updatedMap[notificationId] = newEvent
            _calendarEvents.value = updatedMap

            // Update importance locally
            _notifications.value = _notifications.value.map {
                if (it.id == notificationId) it.copy(isImportant = true) else it
            }

            notificationsRepository.updateNotificationImportance(notificationId, 1)
            eventsRepository.addEventToCalendar(notificationId, newEvent)

            // Send the event to Google Calendar
            val success = googleCalendarApiRepository.upsertEventToGoogleCalendar(notificationId, newEventDetails)
        }
    }

    fun discardEvent(notificationId: String, discardedEventDetails: EventDetails) {
        viewModelScope.launch {
            Log.d("EventDetails", "Discarding event: $discardedEventDetails")

            // Update local state
            val updatedEvent = discardedEventDetails.copy(buttonStatus = 2)
            val updatedMap = _calendarEvents.value.toMutableMap().apply {
                put(notificationId, updatedEvent)
            }
            _calendarEvents.value = updatedMap

            // Mark notification as important locally
            _notifications.value = _notifications.value.map {
                if (it.id == notificationId) it.copy(isImportant = true) else it
            }

            // Update importance in Room
            notificationsRepository.updateNotificationImportance(notificationId, 1)

            // Update local DB
            val localSuccess = eventsRepository.updateEvent(notificationId, updatedEvent)
            if (localSuccess) {
                Log.d("NotificationsViewModel", "Event successfully discarded in Room: $notificationId")
            } else {
                Log.e("NotificationsViewModel", "Failed to discard event locally: $notificationId")
            }

            // ❗️ Delete from Google Calendar
            val googleSuccess = googleCalendarApiRepository.deleteEventFromGoogleCalendar(notificationId)
            if (googleSuccess) {
                Log.d("NotificationsViewModel", "Event deleted from Google Calendar: $notificationId")
            } else {
                Log.e("NotificationsViewModel", "Failed to delete from Google Calendar: $notificationId")
            }
        }
    }


}
