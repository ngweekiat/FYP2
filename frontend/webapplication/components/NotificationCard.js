export default function NotificationCard({ notification, statusMessage, onAdd, onDiscard }) {
    console.log("Notification Data in Card:", notification); // Debugging
  
    return (
      <div className="bg-white shadow-md p-4 rounded-md border border-secondary">
        {/* Sender and Time */}
        <div className="flex justify-between">
          <h2 className="text-primary font-bold">{notification.appName || notification.sender || "Unknown Sender"}</h2>
          <span className="text-secondary">
            {notification.timestamp ? new Date(notification.timestamp).toLocaleString() : "No Timestamp"}
          </span>
        </div>
  
        {/* Notification Title */}
        <h3 className="text-lg font-semibold text-tertiary">{notification.title || "No Title"}</h3>
  
        {/* Notification Content */}
        <p className="text-secondary">{notification.bigText || notification.text || notification.content || "No Content"}</p>
  
        {/* Status Message or Action Buttons */}
        {statusMessage ? (
          <p className="mt-2 text-secondaryDark">{statusMessage}</p>
        ) : notification.button_status === 0 ? (
          <div className="mt-4 flex space-x-2">
            <button onClick={onAdd} className="bg-primary text-white px-4 py-2 rounded">Add</button>
            <button onClick={onDiscard} className="bg-tertiary text-white px-4 py-2 rounded">Discard</button>
          </div>
        ) : null}
      </div>
    );
  }
  