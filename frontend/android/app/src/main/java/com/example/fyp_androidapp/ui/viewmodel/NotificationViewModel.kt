package com.example.fyp_androidapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import com.example.fyp_androidapp.data.models.Notification

class NotificationViewModel : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val client = OkHttpClient()
    private val backendUrl = "http://<your-backend-url>/api/notifications" // Replace with your backend URL

    fun fetchNotifications() {
        viewModelScope.launch {
            try {
                val request = Request.Builder()
                    .url(backendUrl)
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val jsonArray = JSONArray(responseBody)
                        val fetchedNotifications = mutableListOf<Notification>()

                        for (i in 0 until jsonArray.length()) {
                            val jsonNotification = jsonArray.getJSONObject(i)
                            val notification = Notification(
                                sender = jsonNotification.optString("appName"),
                                title = jsonNotification.optString("title"),
                                content = jsonNotification.optString("bigText"),
                                time = jsonNotification.optString("timestamp"),
                                isImportant = jsonNotification.optBoolean("isImportant", false),
                                status = jsonNotification.optString("status", null)
                            )
                            fetchedNotifications.add(notification)
                        }

                        _notifications.value = fetchedNotifications
                    }
                } else {
                    // Log or handle the error response
                }
            } catch (e: Exception) {
                // Handle exceptions
                e.printStackTrace()
            }
        }
    }
}
