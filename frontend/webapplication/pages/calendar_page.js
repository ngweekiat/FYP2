import { useEffect, useState } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";
import axios from "axios";
import { formatDateOnly, formatTimeOnly, formatISOTimestamp } from "../utils/dateUtils"; // ✅ Import new date/time formatters


export default function Calendar() {
  const [events, setEvents] = useState([]);
  const [error, setError] = useState(null);
  const BACKEND_URL = "http://localhost:3000/api/notifications";

  /**
   * Formats event data for FullCalendar
   */
  const formatEvents = (eventsData) =>
    eventsData
      .filter((event) => event.button_status === 1) // Only include saved events
      .map((event) => ({
        id: event.id || "",
        title: event.title || "No Title",
        start: event.start_date || "Unknown Date",
        end: event.end_date || event.start_date,
        description: event.description || "No Description",
        location: event.location || "Unknown Location",
        allDay: event.allDay ?? false,
        startTime: event.start_time || "00:00",
        endTime: event.end_time || "01:00",
      }));

  /**
   * Fetch all events at once from the new /calendar_events_all route
   */
  const fetchEvents = async () => {
    console.log("📅 Fetching all events...");

    try {
      const response = await axios.get(`${BACKEND_URL}/calendar_events_all`); // ✅ Updated API route
      const formattedEvents = formatEvents(response.data.events); // Adjusted to use `events` from response

      console.log("📅 Formatted Events:", formattedEvents);

      setEvents(formattedEvents);
      setError(formattedEvents.length === 0 ? "📭 No events found." : null);
    } catch (error) {
      console.error("🚨 Fetch error:", error.response?.status, error.response?.data);
      setEvents([]);
      setError("🚨 Error fetching events. Please try again.");
    }
  };

  // Fetch events once on mount
  useEffect(() => {
    fetchEvents();
  }, []);

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">Calendar</h1>

      {/* ✅ Display error messages */}
      {error && <p className="text-gray-500 text-center">{error}</p>}

      <FullCalendar
        plugins={[dayGridPlugin, interactionPlugin]}
        initialView="dayGridMonth"
        events={events}
        headerToolbar={{
          left: "prev,next today",
          center: "title",
          right: "dayGridMonth,dayGridWeek,dayGridDay",
        }}
        eventClick={(info) =>
          alert(`Event: ${info.event.title}\nDetails: ${info.event.extendedProps.description}`)
        }
      />
    </div>
  );
}
