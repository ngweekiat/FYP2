package com.example.fyp_androidapp;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.example.fyp_androidapp.database.entities.NotificationEntity
import android.util.Log;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.launch;
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.security.MessageDigest
import com.example.fyp_androidapp.database.AppDatabase
import com.example.fyp_androidapp.database.DatabaseProvider
import com.example.fyp_androidapp.utils.NotificationETL



fun generateHashedId(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }.take(16) // Use first 16 chars for brevity
}

class NotificationListener : NotificationListenerService() {
    private val TAG = "NotificationListener"
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    val database = DatabaseProvider.getDatabase()


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 NotificationListener service created")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        // Extract notification details
        val packageName = sbn.packageName

        val tag = sbn.tag
        val key = sbn.key
        val groupKey = sbn.groupKey
        val group = sbn.notification.group
        val whenTime = sbn.notification.`when` // Specific timestamp for when the notification was created
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

        if (isGroupSummary) {
            Log.d(TAG, "🧺 Group summary notification ignored: ${sbn.key}")
            return
        }
        val actions = notification.actions

        // Generate unique ID from the hash value of rawID
        val rawId = "$key|$whenTime"
        val id = generateHashedId(rawId)

        // Fetch application name safely
        val appName = try {
            val packageManager = packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching app name for package: $packageName, ${e.message}")
            packageName // Fallback to package name
        }

        Log.d(TAG, "App Name: $appName")


        // Save to Room DB
        serviceScope.launch {
            try {
                val existing = database.notificationDao().getNotificationById(id)
                if (existing != null) {
                    Log.d(TAG, "🔁 Duplicate notification ignored: $id")
                    return@launch Unit
                }

                val entity = NotificationEntity(
                    id = id,
                    packageName = packageName,
                    appName = appName,
                    title = title,
                    text = text,
                    subText = subText,
                    infoText = infoText,
                    summaryText = summaryText,
                    bigText = bigText,
                    category = category,
                    showWhen = showWhen.toString(),
                    channelId = channelId,
                    peopleList = peopleList?.joinToString(","),
                    template = template,
                    remoteInputHistory = remoteInputHistory?.joinToString(","),
                    visibility = visibility.toString(),
                    priority = priority.toString(),
                    flags = flags.toString(),
                    color = color?.toString(),
                    sound = sound,
                    vibrate = vibrate,
                    audioStreamType = audioStreamType.toString(),
                    contentView = contentView,
                    bigContentView = bigContentView,
                    groupKey = groupKey,
                    group = group,
                    overrideGroupKey = overrideGroupKey,
                    isOngoing = isOngoing.toString(),
                    isClearable = isClearable.toString(),
                    userHandle = userHandle,
                    timestamp = whenTime,
                    isImportant = false
                )
                database.notificationDao().insert(entity)
                Log.d(TAG, "✅ Notification saved to Room DB: $id")


                // Call the notification processor
                NotificationETL.processNotificationImportance(database, entity)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to save notification: ${e.message}")
            }
        }
    }
}
