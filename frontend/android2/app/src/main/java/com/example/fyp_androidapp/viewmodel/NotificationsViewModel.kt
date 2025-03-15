package com.example.fyp_androidapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp_androidapp.data.models.EventDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.fyp_androidapp.data.models.Notification
import com.example.fyp_androidapp.data.repository.EventsRepository
import com.example.fyp_androidapp.data.repository.NotificationsRepository

class NotificationsViewModel(
    private val repository: NotificationsRepository = NotificationsRepository(),
    private val EventsRepository: EventsRepository = EventsRepository()
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _calendarEvents = MutableStateFlow<Map<String, EventDetails>>(emptyMap())
    val calendarEvents: StateFlow<Map<String, EventDetails>> = _calendarEvents

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var lastVisibleNotificationId: String? = null

    // Fetch notifications from backend with pagination
    fun fetchNotifications(limit: Int = 20) {
        if (_isLoading.value) return // Prevent duplicate requests

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val newNotifications = repository.fetchNotifications(limit, lastVisibleNotificationId)

                // Get notifications that are important
                val importantNotifications = newNotifications.filter { it.isImportant }

                // Fetch calendar events for important notifications
                fetchcalendarevents(importantNotifications)

                if (newNotifications.isNotEmpty()) {
                    lastVisibleNotificationId = newNotifications.last().id
                }

                // Store the notifications
                _notifications.value = _notifications.value + newNotifications

                // Filter important notifications




            } catch (e: Exception) {
                // Handle the error (e.g., show a message to the user)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchcalendarevents(notifications: List<Notification>) {
        viewModelScope.launch(Dispatchers.IO) {
            val eventsMap = mutableMapOf<String, EventDetails>()

            for (notification in notifications) {
                val event = EventsRepository.fetchEventDetails(notification.id)
                if (event != null) {
                    eventsMap[notification.id] = event
                }
            }
            _calendarEvents.value = eventsMap
        }
    }




}
