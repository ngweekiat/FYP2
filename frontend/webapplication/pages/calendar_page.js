import { useEffect, useState } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";
import axios from "axios";
import EventPopupDialog from "../components/EventPopupDialog"; 
import { convertToISO } from "../utils/dateUtils";
import { handleSaveEvent, handleDiscardEvent } from "../utils/eventHandlers";


export default function Calendar() {
  const [events, setEvents] = useState([]);
  const [error, setError] = useState(null);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const BACKEND_URL = "http://localhost:3000/api/notifications";

  const formatEvents = (eventsData) =>
    eventsData
      .filter((event) => event.button_status === 1)
      .map((event) => {
        const startDateTime = convertToISO(event.start_date, event.start_time);
        const endDateTime = event.end_date ? convertToISO(event.end_date, event.end_time) : startDateTime;

        return {
          id: event.id || "",
          title: event.title || "No Title",
          start: startDateTime,
          end: endDateTime,
          description: event.description || "No Description",
          location: event.location || "Unknown Location",
          allDay: event.allDay ?? false,
        };
      });

  const fetchEvents = async () => {
    console.log("📅 Fetching all events...");
    try {
      const response = await axios.get(`${BACKEND_URL}/calendar_events_all`);
      const formattedEvents = formatEvents(response.data.events);
      setEvents(formattedEvents);
      setError(formattedEvents.length === 0 ? "📭 No events found." : null);
    } catch (error) {
      console.error("🚨 Fetch error:", error.response?.status, error.response?.data);
      setEvents([]);
      setError("🚨 Error fetching events. Please try again.");
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  const handleEventClick = (info) => {
    setSelectedEvent({
      id: info.event.id,
      title: info.event.title,
      description: info.event.extendedProps.description,
      startDate: info.event.start ? info.event.start.toISOString().split("T")[0] : "",
      startTime: info.event.start ? info.event.start.toISOString().split("T")[1].substring(0, 5) : "",
      endDate: info.event.end ? info.event.end.toISOString().split("T")[0] : "",
      endTime: info.event.end ? info.event.end.toISOString().split("T")[1].substring(0, 5) : "",
      location: info.event.extendedProps.location || "Unknown Location",
    });
    setIsDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setIsDialogOpen(false);
    setSelectedEvent(null);
  };

  const handleSaveEventWrapper = (updatedEvent) => {
    handleSaveEvent(updatedEvent, setEvents, setIsDialogOpen);
    fetchEvents(); // Refetch events after saving
  };
  
  const handleDiscardEventWrapper = (eventId) => {
    handleDiscardEvent(eventId, setEvents, setIsDialogOpen);
    fetchEvents(); // Refetch events after saving
  };
  

  return (
    <div className="p-6 relative min-h-screen bg-gray-100 flex flex-col items-center">
      <h1 className="text-3xl font-bold mb-6 text-gray-800">📅 Event Calendar</h1>

      {error && <p className="text-red-500 text-center">{error}</p>}

      <div
        className={`transition-opacity duration-300 ${
          isDialogOpen ? "pointer-events-none opacity-50" : ""
        } w-full max-w-5xl bg-white rounded-xl shadow-lg p-4`}
      >
        <FullCalendar
          plugins={[dayGridPlugin, interactionPlugin]}
          initialView="dayGridMonth"
          events={events}
          headerToolbar={{
            left: "prev,next today",
            center: "title",
            right: "dayGridMonth,dayGridWeek,dayGridDay",
          }}
          eventTimeFormat={{
            hour: "numeric",
            minute: "2-digit",
            hour12: true,
          }}
          eventClick={handleEventClick}
          eventClassNames={() => "bg-blue-500 text-white rounded-md p-1"} // ✅ Better event styling
          height="auto"
        />
      </div>

      {isDialogOpen && (
        <div className="fixed inset-0 flex items-center justify-centerz-50">
          <EventPopupDialog
            eventDetails={selectedEvent}
            onAdd={handleSaveEventWrapper}
            onDiscard={handleDiscardEventWrapper}
            onClose={handleCloseDialog}
          />
        </div>
      )}
    </div>
  );
}
