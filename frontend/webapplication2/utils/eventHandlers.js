// 📁 utils/eventHandlers.js
import { createGoogleCalendarEvent, deleteGoogleCalendarEvent } from "./googleCalendarClient";

/**
 * Handles saving an event and syncing with Google Calendar.
 */
export const handleSaveEvent = async (updatedEvent, setEvents, setShowPopup, accessToken) => {
  try {
    console.log("📩 [DEBUG] Event Data Before Sending:", JSON.stringify(updatedEvent, null, 2));

    const formatTime = (time) => (time && time.length === 5 ? `${time}:00` : time || "00:00:00");

    const eventDetails = {
      title: updatedEvent.title,
      description: updatedEvent.description || "",
      location: updatedEvent.location || "",
      startDate: updatedEvent.start_date,
      startTime: formatTime(updatedEvent.start_time),
      endDate: updatedEvent.end_date || updatedEvent.start_date,
      endTime: formatTime(updatedEvent.end_time || updatedEvent.start_time),
      attendees: Array.isArray(updatedEvent.attendees) ? updatedEvent.attendees : [],
    };

    console.log("📤 Creating Google Calendar Event with details:", JSON.stringify(eventDetails, null, 2));
    console.log("🔐 Using Access Token:", accessToken);

    const calendarResult = await createGoogleCalendarEvent(eventDetails, accessToken);

    if (calendarResult && calendarResult.id) {
      setEvents((prevEvents) =>
        prevEvents.map((e) =>
          e.id === updatedEvent.id ? { ...updatedEvent, button_status: 1, id: calendarResult.id } : e
        )
      );
      

      console.log(`✅ Event created: ${calendarResult.id} (Synced with Google Calendar)`);
    }
  } catch (error) {
    console.error("🚨 Error saving event:", error.message || error);
  } finally {
    setShowPopup(false);
  }
};

/**
 * Handles discarding an event from Google Calendar.
 */
export const handleDiscardEvent = async (eventId, setEvents, setShowPopup, accessToken) => {
  try {
    const deleted = await deleteGoogleCalendarEvent(eventId, accessToken);

    if (deleted) {
      setEvents((prevEvents) => ({
        ...prevEvents,
        [eventId]: { ...prevEvents[eventId], button_status: 2 },
      }));

      console.log(`❌ Event discarded: ${eventId} (Removed from Google Calendar)`);
    }
  } catch (error) {
    console.error("🚨 Error discarding event:", error.message || error);
  } finally {
    setShowPopup(false);
  }
};
