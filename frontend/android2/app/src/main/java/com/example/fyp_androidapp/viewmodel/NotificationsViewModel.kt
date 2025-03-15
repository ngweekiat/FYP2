package com.example.fyp_androidapp.viewmodel

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
}
