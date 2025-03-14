import { formatDate } from "../utils/dateUtils"; // ✅ Import date formatter

export default function NotificationCard({ notification, event, onEventClick, onAddEvent, onDiscardEvent }) {
  console.log(`🛑 Notification ID: ${notification.id}, Passed Event:`, event); // ✅ Debugging log

  return (
    <div className="bg-white shadow-md p-4 rounded-md border border-gray-300">
      {/* Sender and Time */}
      <div className="flex justify-between items-center">
        <h2 className="text-primary font-bold text-lg">
          {notification.appName || notification.sender || "Unknown Sender"}
        </h2>
        <span className="text-gray-500 text-sm">{formatDate(notification.timestamp)}</span>
      </div>

      {/* Notification Title */}
      {notification.title && (
        <h3 className="text-md font-bold text-gray-900 mt-1">{notification.title}</h3>
      )}

      {/* Notification Content */}
      <p className="text-gray-600 text-sm mt-1">
        {notification.bigText || notification.text || notification.content || "No Content"}
      </p>

      {/* Extracted Event Section Based on Conditions */}
      {notification.notification_importance === 1 && (
        <div className="mt-3 p-3 border border-gray-300 bg-gray-100 rounded-md shadow-sm">
          {event.button_status === 0 && (
            <div className="flex justify-between">
              <button
                className="bg-blue-500 text-white p-2 rounded-md hover:bg-blue-600 transition"
                onClick={() => onAddEvent(event)}
              >
                Add Event
              </button>
              <button
                className="bg-red-500 text-white p-2 rounded-md hover:bg-red-600 transition"
                onClick={() => onDiscardEvent(event)}
              >
                Discard Event
              </button>
            </div>
          )}

          {event.button_status === 1 && event.title && event.start_date && event.start_time && (
            <button
              className="w-full p-3 border border-gray-300 bg-gray-100 rounded-md shadow-sm text-left hover:bg-gray-200 active:bg-gray-300 transition"
              onClick={() => onEventClick && onEventClick(event)}
            >
              <p className="text-sm text-gray-800 font-bold">{event.title}</p>
              <p className="text-sm text-gray-800 font-bold">
                {formatDate(`${event.start_date} ${event.start_time}`)}
              </p>
            </button>
          )}

          {event.button_status === 2 && (
            <div className="w-full p-3 border border-gray-300 bg-gray-100 rounded-md shadow-sm text-left">
              <p className="text-sm text-red-700 font-bold">Event Discarded</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}