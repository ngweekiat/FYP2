import axios from "axios";

const BACKEND_URL = "http://localhost:3000/api/notifications";
const GOOGLE_CALENDAR_URL = "http://localhost:3000/api/google-calendar";

/**
 * Handles saving an event, updating both the local database and Google Calendar.
 * If the event does not exist, it creates a new event.
 *
 * @param {Object} updatedEvent - The updated event data.
 * @param {Function} setEvents - State setter function for events.
 * @param {Function} setShowPopup - State setter function for closing popup.
 */
export const handleSaveEvent = async (updatedEvent, setEvents, setShowPopup) => {
  try {
    const eventId = updatedEvent.id;

    console.log("📩 [DEBUG] Event Data Before Sending:", JSON.stringify(updatedEvent, null, 2));

    
    console.log(`🟢 Checking if event ${eventId} exists...`);

    // First, check if the event exists in the database
    let eventExists = false;

    try {
      await axios.get(`${BACKEND_URL}/calendar_events/${eventId}`);
      eventExists = true;
      console.log(`✅ Event ${eventId} exists. Updating...`);
    } catch (err) {
      if (err.response && err.response.status === 404) {
        console.warn(`⚠️ Event ${eventId} not found. Creating a new event.`);
      } else {
        throw err; // Rethrow any unexpected errors
      }
    }

    let response;
    if (eventExists) {
      // Update existing event
      response = await axios.patch(`${BACKEND_URL}/calendar_events/${eventId}`, {
        title: updatedEvent.title,
        start_date: updatedEvent.start_date,
        start_time: updatedEvent.start_time,
        end_date: updatedEvent.end_date || updatedEvent.start_date,
        end_time: updatedEvent.end_time || updatedEvent.start_time,
        location: updatedEvent.location || "",
        description: updatedEvent.description || "",
        button_status: 1, // ✅ Mark as saved
      });
    } else {
      // Create a new event
      response = await axios.post(`${BACKEND_URL}/calendar_events`, {
        id: eventId,
        title: updatedEvent.title,
        start_date: updatedEvent.start_date,
        start_time: updatedEvent.start_time,
        end_date: updatedEvent.end_date || updatedEvent.start_date,
        end_time: updatedEvent.end_time || updatedEvent.start_time,
        location: updatedEvent.location || "",
        description: updatedEvent.description || "",
        button_status: 1, // ✅ Mark as saved
      });
    }

    console.log(`📅 Event saved/updated successfully: ${eventId}`, response.data);

    // Upsert the event in Google Calendar
    const googleCalendarResponse = await axios.put(`${GOOGLE_CALENDAR_URL}/upsert-event`, {
      eventId,
      eventDetails: {
        title: updatedEvent.title,
        startDate: updatedEvent.start_date,
        startTime: updatedEvent.start_time,
        endDate: updatedEvent.end_date || updatedEvent.start_date,
        endTime: updatedEvent.end_time || updatedEvent.start_time,
        locationOrMeeting: updatedEvent.location || "",
        description: updatedEvent.description || "",
        attendees: updatedEvent.attendees || [],
      },
    });

    if (googleCalendarResponse.status === 200) {
      setEvents((prevEvents) => ({
        ...prevEvents,
        [eventId]: { ...updatedEvent, button_status: 1 }, // ✅ Change status to saved
      }));

      console.log(`✅ Event added/updated: ${eventId} (Synced with Google Calendar)`);
    } else {
      throw new Error("Failed to upsert event to Google Calendar");
    }
  } catch (error) {
    console.error("🚨 Error saving event:", error.message || error);
  } finally {
    setShowPopup(false); // ✅ Close the popup after saving
  }
};

/**
 * Handles discarding an event, removing it from both the local database and Google Calendar.
 * @param {string} eventId - The ID of the event to discard.
 * @param {Function} setEvents - State setter function for events.
 * @param {Function} setShowPopup - State setter function for closing popup.
 */
export const handleDiscardEvent = async (eventId, setEvents, setShowPopup) => {
  try {
    // Mark event as discarded in the local database
    await axios.patch(`${BACKEND_URL}/calendar_events/${eventId}`, {
      button_status: 2, // ✅ Mark as discarded
    });

    // Delete event from Google Calendar (Fix: Ensure correct JSON format)
    const googleCalendarResponse = await axios.delete(`${GOOGLE_CALENDAR_URL}/delete-event`, {
      data: { eventId }, // Ensure eventId is in the request body
      headers: { "Content-Type": "application/json" },
    });

    if (googleCalendarResponse.status === 200) {
      setEvents((prevEvents) => ({
        ...prevEvents,
        [eventId]: { ...prevEvents[eventId], button_status: 2 }, // ✅ Change status to discarded
      }));

      console.log(`❌ Event discarded: ${eventId} (Removed from Google Calendar)`);
    } else {
      throw new Error("Failed to delete event from Google Calendar");
    }
  } catch (error) {
    console.error("🚨 Error discarding event:", error.message || error);
  } finally {
    setShowPopup(false); // ✅ Close the popup after discarding
  }
};
