import { formatDateOnly, formatTimeOnly, formatISOTimestamp } from "../utils/dateUtils"; // ✅ Import new date/time formatters

export default function NotificationCard({ notification, event, onEventClick, onAddEvent, onDiscardEvent }) {
  return (
    <div className="bg-white shadow-md p-4 rounded-md border border-gray-300">
      {/* Sender and Time */}
      <div className="flex justify-between items-center">
        <h2 className="text-primary font-bold text-lg">
          {notification.appName || notification.sender || "Unknown Sender"}
        </h2>
        <span className="text-gray-500 text-sm">
          {formatDateOnly(notification.timestamp)} • {formatISOTimestamp(notification.timestamp)}
        </span>
      </div>

      {/* Notification Title */}
      {notification.title && (
        <h3 className="text-md font-bold text-gray-900 mt-1">{notification.title}</h3>
      )}

      {/* Notification Content */}
      <p className="text-gray-600 text-sm mt-1">
        {notification.bigText || notification.text || notification.content || "No Content"}
      </p>

      {/* Only render event section if event exists */}
      {event && (
        <div className="mt-3 p-3 border border-gray-300 bg-gray-100 rounded-md shadow-sm">
          {event.button_status === 0 && (
            <div className="flex gap-3">
              <button
                className="bg-blue-500 text-white px-4 py-3 text-sm font-bold rounded-md hover:bg-blue-600 transition w-full"
                onClick={() => onAddEvent(event)}
              >
                Add Event
              </button>
              <button
                className="bg-red-800 text-white px-4 py-3 text-sm font-bold rounded-md hover:bg-red-900 transition w-full"
                onClick={() => onDiscardEvent(event.id)}
              >
                Discard Event
              </button>
            </div>
          )}
          {/* Display extracted event details with a black bar in front */}
          {event.button_status === 1 && event.title && event.start_date && event.start_time && (
            <div
              className="flex items-center gap-3 mt-2 cursor-pointer" // Add cursor-pointer for indication
              onClick={() => onAddEvent(event)} // Trigger the popup when clicked
            >
              {/* Black vertical bar */}
              <div className="w-[4px] min-h-[20px] bg-blue-500 self-stretch shrink-0"></div>
              <div>
                <p className="text-sm font-bold text-blue-500">{event.title}</p>
                <p className="text-sm font-bold text-blue-500">
                  {formatDateOnly(event.start_date)} {formatTimeOnly(event.start_time)}
                </p>
              </div>
            </div>
          )}

          {/* Display "Event Discarded" with black bar for button_status === 2 */}
          {event.button_status === 2 && (
            <div
              className="flex items-center gap-3 mt-2 cursor-pointer" // Add cursor-pointer for indication
              onClick={() => onAddEvent(event)} // Trigger the popup when clicked
            >
              <div className="w-[4px] min-h-[40px] bg-red-800 self-stretch shrink-0"></div>
              <p className="text-sm font-bold text-red-800">Event Discarded</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
