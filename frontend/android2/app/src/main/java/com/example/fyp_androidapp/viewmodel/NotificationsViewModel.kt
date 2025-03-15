package com.example.fyp_androidapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.fyp_androidapp.data.models.Notification
import com.example.fyp_androidapp.data.repository.NotificationsRepository

class NotificationsViewModel(private val repository: NotificationsRepository = NotificationsRepository()) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

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

                if (newNotifications.isNotEmpty()) {
                    lastVisibleNotificationId = newNotifications.last().id
                }

                _notifications.value = _notifications.value + newNotifications
            } catch (e: Exception) {
                // Handle the error (e.g., show a message to the user)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
