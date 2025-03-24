import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: String, // Assuming 'id' is a unique String
    val sender: String,
    val title: String,
    val content: String,
    val time: String,
    val isImportant: Boolean,
)