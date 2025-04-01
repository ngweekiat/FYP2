const CALENDAR_API = "https://www.googleapis.com/calendar/v3/calendars/primary/events";

/**
 * Creates a new Google Calendar event from the frontend using the access token.
 */
export async function createGoogleCalendarEvent(eventDetails, accessToken) {
  try {
    const headers = {
      Authorization: `Bearer ${accessToken}`,
      "Content-Type": "application/json",
    };

    const eventPayload = {
      summary: eventDetails.title || "Untitled Event",
      description: eventDetails.description || "",
      location: eventDetails.location || "",
      start: {
        dateTime: `${eventDetails.startDate}T${eventDetails.startTime}`,
        timeZone: "Asia/Singapore",
      },
      end: {
        dateTime: `${eventDetails.endDate}T${eventDetails.endTime}`,
        timeZone: "Asia/Singapore",
      },
    };

    if (Array.isArray(eventDetails.attendees) && eventDetails.attendees.length > 0) {
      eventPayload.attendees = eventDetails.attendees;
    }

    // 🔍 Log full request
    console.log("📤 Sending FULL request to Google Calendar API (create):");
    console.log("🔐 Access Token:", accessToken);
    console.log("📦 Body:", JSON.stringify(eventPayload, null, 2));

    const res = await fetch(CALENDAR_API, {
      method: "POST",
      headers,
      body: JSON.stringify(eventPayload),
    });

    const data = await res.json();

    if (res.ok) {
      console.log("✅ Event created successfully:", data);
      return data;
    } else {
      console.error("❌ Google Calendar API error response:", data);
      throw new Error(`❌ Failed to create event: ${data.error.message}`);
    }
  } catch (err) {
    console.error("🚨 Google Calendar API Error:", err.message);
    throw err;
  }
}



/**
 * Deletes a Google Calendar event from the frontend.
 */
export async function deleteGoogleCalendarEvent(eventId, accessToken) {
  try {
    const res = await fetch(`${CALENDAR_API}/${eventId}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });

    if (res.status === 204) {
      console.log("🗑️ Event deleted successfully");
      return true;
    } else if (res.status === 404) {
      console.warn("⚠️ Event not found.");
      return false;
    } else {
      const errorData = await res.json();
      throw new Error(`❌ Failed to delete event: ${errorData.error.message}`);
    }
  } catch (err) {
    console.error("🚨 Delete Error:", err.message);
    throw err;
  }
}
