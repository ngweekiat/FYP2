package com.example.fyp_androidapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp_androidapp.data.models.EventDetails
import com.example.fyp_androidapp.data.models.Notification
import com.example.fyp_androidapp.data.repository.EventsRepository
import com.example.fyp_androidapp.data.repository.NotificationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class NotificationsViewModel(
    private val notificationsRepository: NotificationsRepository = NotificationsRepository(),
    private val eventsRepository: EventsRepository = EventsRepository()
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _calendarEvents = MutableStateFlow<Map<String, EventDetails>>(emptyMap())
    val calendarEvents: StateFlow<Map<String, EventDetails>> = _calendarEvents

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var lastVisibleNotificationId: String? = null

    fun fetchNotifications(limit: Int = 20) {
        if (_isLoading.value) return

        _isLoading.value = true
        viewModelScope.launch {
            val (newNotifications, lastVisibleId) = notificationsRepository.fetchNotifications(lastVisibleNotificationId, limit)

            if (newNotifications.isNotEmpty()) {
                _notifications.value = _notifications.value + newNotifications
                lastVisibleNotificationId = lastVisibleId

                // Fetch event details only for important notifications
                newNotifications.filter { it.isImportant }.forEach { notification ->
                    fetchCalendarEvent(notification.id)
                }
            }

            _isLoading.value = false
        }
    }

    fun fetchCalendarEvent(notificationId: String) {
        viewModelScope.launch {
            val eventDetails = eventsRepository.fetchCalendarEvent(notificationId)
            if (eventDetails != null) {
                _calendarEvents.value = _calendarEvents.value + (notificationId to eventDetails)
            }
        }
    }

    fun addEvent(notificationId: String, newEventDetails: EventDetails) {
        viewModelScope.launch {
            // Update UI immediately
            val newEvent = newEventDetails.copy(buttonStatus = 1) // ✅ Ensure buttonStatus is set
            Log.d("EventDetails", newEventDetails.toString())

            // Force recomposition by creating a new map instance
            val newMap = _calendarEvents.value.toMutableMap().apply {
                put(notificationId, newEvent)
            }
            _calendarEvents.value = newMap.toMap() // ✅ Ensure reactivity

            // Update notification importance locally
            _notifications.value = _notifications.value.map {
                if (it.id == notificationId) it.copy(isImportant = true) else it
            }

            // Send update to backend
            notificationsRepository.updateNotificationImportance(notificationId, 1)

            // Add event to the calendar
            eventsRepository.addEventToCalendar(notificationId, newEvent)
        }
    }



    fun discardEvent(notificationId: String) {
        viewModelScope.launch {
            val updatedEvent = eventsRepository.discardEvent(notificationId)?.copy(buttonStatus = 2) // 🔥 Ensure status is set
            if (updatedEvent != null) {
                // Force a new instance to trigger recomposition
                val newMap = _calendarEvents.value.toMutableMap().apply {
                    put(notificationId, updatedEvent)
                }
                _calendarEvents.value = newMap.toMap() // Ensure it's a new object
                //                Log.d("ViewModel", "Event Discarded for Notification ID: $notificationId")
            }
        }
    }
}
