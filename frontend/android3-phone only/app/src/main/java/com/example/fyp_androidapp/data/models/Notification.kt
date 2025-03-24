import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity(tableName = "notifications")
data class NotificationData(
    @PrimaryKey val id: String, // Unique ID
    val packageName: String?,
    val appName: String?,
    val title: String?,
    val text: String?,
    val subText: String?,
    val infoText: String?,
    val summaryText: String?,
    val bigText: String?,
    val category: String?,
    val showWhen: Boolean?,
    val channelId: String?,
    val people: String?, // Store as JSON string or convert to List if needed
    val template: String?,
    val remoteInputHistory: String?, // Store as JSON string or convert to List if needed
    val timestamp: String?,
    val tag: String?,
    val key: String?,
    val groupKey: String?,
    val overrideGroupKey: String?,
    val group: String?,
    val isOngoing: Boolean?,
    val isClearable: Boolean?,
    val userHandle: String?,
    val visibility: Int?,
    val priority: Int?,
    val flags: Int?,
    val color: Int?,
    val sound: String?,
    val vibrate: String?,
    val audioStreamType: Int?,
    val contentView: String?,
    val bigContentView: String?,
    val isGroupSummary: Boolean?,
    val actions: String? // Store actions as JSON string if needed
)