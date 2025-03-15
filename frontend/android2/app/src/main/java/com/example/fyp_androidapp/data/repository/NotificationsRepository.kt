package com.example.fyp_androidapp.data.repository

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.example.fyp_androidapp.Constants
import com.example.fyp_androidapp.data.models.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationsRepository {

    private val client = OkHttpClient()

    // Function to fetch notifications from the backend with pagination
    suspend fun fetchNotifications(limit: Int, startAfter: String? = null): List<Notification> {
        val url = buildUrl(limit, startAfter)
        val request = Request.Builder().url(url).get().build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                val notificationsArray = json.getJSONArray("notifications")
                val notifications = mutableListOf<Notification>()

                for (i in 0 until notificationsArray.length()) {
                    val item = notificationsArray.getJSONObject(i)
                    val notification = Notification(
                        id = item.optString("id", ""),
                        sender = item.optString("appName", "Unknown"),
                        title = item.optString("title", "No Title"),
                        content = item.optString("bigText", item.optString("text", "No Content")),
                        time = item.optString("timestamp", "Unknown Time"),
                        isImportant = item.optInt("notification_importance", 0) == 1,
                        button_status = item.optInt("button_status", 0)
                    )
                    notifications.add(notification)
                }

                return@withContext notifications
            } else {
                return@withContext emptyList<Notification>() // Return empty list if the request fails
            }
        }
    }

    private fun buildUrl(limit: Int, startAfter: String?): String {
        val url = StringBuilder("${Constants.BASE_URL}/notifications?limit=$limit")
        startAfter?.let {
            url.append("&startAfter=$it")
        }
        return url.toString()
    }
}
