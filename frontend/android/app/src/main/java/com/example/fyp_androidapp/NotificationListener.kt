package com.example.fyp_androidapp;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.launch;
import okhttp3.MediaType.Companion.toMediaTypeOrNull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter



class NotificationListener : NotificationListenerService() {

    private val TAG = "NotificationListener"
    private val BACKEND_URL = "http://172.20.10.3:3000/api/notifications" // Replace with your backend URL
    private val client = OkHttpClient()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        // Extract notification details
        val packageName = sbn.packageName
        val id = sbn.id
        val tag = sbn.tag
        val key = sbn.key
        val groupKey = sbn.groupKey
        val group = sbn.notification.group
        val postTime = sbn.postTime
        val iso8601PostTime = Instant.ofEpochMilli(postTime)//Convert to iso time
            .atZone(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val isOngoing = sbn.isOngoing
        val isClearable = sbn.isClearable
        val overrideGroupKey = sbn.overrideGroupKey
        val userHandle = sbn.user.toString()

        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString()
        val summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val category = notification.category
        val showWhen = extras.getBoolean(Notification.EXTRA_SHOW_WHEN, false)
        val channelId = extras.getString(Notification.EXTRA_CHANNEL_ID)
        val peopleList = extras.getStringArrayList(Notification.EXTRA_PEOPLE_LIST)?.toList() // Updated
        val template = extras.getString(Notification.EXTRA_TEMPLATE)
        val remoteInputHistory = extras.getCharSequenceArray(Notification.EXTRA_REMOTE_INPUT_HISTORY)?.map { it.toString() }

        val visibility = notification.visibility
        val priority = notification.priority
        val flags = notification.flags

        val color = notification.color
        val sound = notification.sound?.toString()
        val vibrate = notification.vibrate?.joinToString(",")
        val audioStreamType = notification.audioStreamType

        val contentView = notification.contentView?.toString()
        val bigContentView = notification.bigContentView?.toString()

        val isGroupSummary = (flags and Notification.FLAG_GROUP_SUMMARY) != 0

        val actions = notification.actions

        // Get the full application name
        val appName = try {
            val packageManager = packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching app name for package: $packageName, ${e.message}")
            packageName // Fallback to package name if app name can't be retrieved
        }

        Log.d(TAG, "App Name: $appName")

        // Build JSON object
        val notificationData = JSONObject()
        try {
            notificationData.put("packageName", packageName)
            notificationData.put("appName", appName)
            notificationData.put("title", title)
            notificationData.put("text", text)
            notificationData.put("subText", subText)
            notificationData.put("infoText", infoText)
            notificationData.put("summaryText", summaryText)
            notificationData.put("bigText", bigText)
            notificationData.put("category", category)
            notificationData.put("showWhen", showWhen)
            notificationData.put("channelId", channelId)
            notificationData.put("people", JSONArray(peopleList)) // Updated
            notificationData.put("template", template)
            notificationData.put("remoteInputHistory", JSONArray(remoteInputHistory))
            notificationData.put("timestamp", iso8601PostTime)
            notificationData.put("id", id)
            notificationData.put("tag", tag)
            notificationData.put("key", key)
            notificationData.put("groupKey", groupKey)
            notificationData.put("overrideGroupKey", overrideGroupKey)
            notificationData.put("group", group)
            notificationData.put("isOngoing", isOngoing)
            notificationData.put("isClearable", isClearable)
            notificationData.put("userHandle", userHandle)
            notificationData.put("visibility", visibility)
            notificationData.put("priority", priority)
            notificationData.put("flags", flags)
            notificationData.put("color", color)
            notificationData.put("sound", sound)
            notificationData.put("vibrate", vibrate)
            notificationData.put("audioStreamType", audioStreamType)
            notificationData.put("contentView", contentView)
            notificationData.put("bigContentView", bigContentView)
            notificationData.put("isGroupSummary", isGroupSummary)

            // Add actions as a JSON array if available
            if (actions != null) {
                val actionsArray = JSONArray()
                for (action in actions) {
                    val actionData = JSONObject()
                    actionData.put("title", action.title)
                    actionData.put("actionIntent", action.actionIntent?.toString())
                    actionsArray.put(actionData)
                }
                notificationData.put("actions", actionsArray)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error creating JSON object: ${e.message}")
            return
        }

        // Send data to backend
        sendToBackend(notificationData)
    }


    private fun sendToBackend(notificationData: JSONObject) {
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = RequestBody.create(JSON, notificationData.toString())

        Log.d(TAG, "Sending Data to Backend: $notificationData") // Log the JSON object being sent

        val request = Request.Builder()
            .url(BACKEND_URL)
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Log.d(TAG, "Notification sent successfully: ${response.body?.string()}")
                } else {
                    Log.e(TAG, "Failed to send notification: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending notification: ${e.message}")
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

        // Log notification removal
        Log.d(TAG, "Notification Removed: Package=${sbn.packageName}")
    }
}
