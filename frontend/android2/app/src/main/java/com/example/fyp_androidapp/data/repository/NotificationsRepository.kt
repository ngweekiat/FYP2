package com.example.fyp_androidapp.data.repository

import android.util.Log
import com.example.fyp_androidapp.Constants
import com.example.fyp_androidapp.data.models.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificationsRepository {

    private val client = OkHttpClient()
    private val backendUrl = "${Constants.BASE_URL}/notifications"

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mma")

    private fun formatTimestampToSGT(timestamp: String): String {
        return try {
            val zonedDateTime = ZonedDateTime.parse(timestamp)
                .withZoneSameInstant(ZoneId.of("Asia/Singapore"))
            zonedDateTime.format(timeFormatter).uppercase()
        } catch (e: Exception) {
            "Unknown Time"
        }
    }

    suspend fun updateNotificationImportance(notificationId: String, importance: Int) {
        withContext(Dispatchers.IO) {
            try {
                val url = "$backendUrl/$notificationId/updateImportance"
                val requestBody = JSONObject().apply {
                    put("notification_importance", importance)
                }.toString()

                val request = Request.Builder()
                    .url(url)
                    .put(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e("NotificationsRepo", "Failed to update importance for $notificationId")
                } else {
                    Log.d("NotificationsRepo", "Successfully updated importance for $notificationId")
                }
            } catch (e: Exception) {
                Log.e("NotificationsRepo", "Error updating importance", e)
            }
        }
    }


    suspend fun fetchNotifications(lastVisible: String?, limit: Int = 20): Pair<List<Notification>, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val url = StringBuilder("$backendUrl?limit=$limit")
                if (lastVisible != null) url.append("&startAfter=$lastVisible")

                val request = Request.Builder().url(url.toString()).get().build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val json = JSONObject(responseBody)
                    val notificationsArray = json.getJSONArray("notifications")
                    val newNotifications = mutableListOf<Notification>()

                    for (i in 0 until notificationsArray.length()) {
                        val item = notificationsArray.getJSONObject(i)
                        val notification = Notification(
                            id = item.optString("id", ""),
                            sender = item.optString("appName", "Unknown"),
                            title = item.optString("title", "No Title"),
                            content = item.optString("bigText", item.optString("text", "No Content")),
                            time = formatTimestampToSGT(item.optString("timestamp", "Unknown Time")),
                            isImportant = item.optInt("notification_importance", 0) == 1,
                        )
                        newNotifications.add(notification)
                    }

                    val lastVisibleId = json.optString("lastVisible", null)

                    Pair(newNotifications, lastVisibleId)
                } else {
                    Pair(emptyList(), null)
                }
            } catch (e: Exception) {
                Pair(emptyList(), null) // Return empty list on failure
            }
        }
    }
}
