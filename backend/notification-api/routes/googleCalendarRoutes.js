const express = require('express');
const router = express.Router();
const { addEvent } = require('../config/googleCalendar');

/**
 * Route: POST /add-event
 * Adds an event to a user's Google Calendar.
 */
/**
 * Route: POST /add-event
 * Adds an event to a user's Google Calendar.
 */
router.post('/add-event', async (req, res) => {
    const { userId, eventDetails } = req.body;

    if (!userId || !eventDetails || !eventDetails.id || !eventDetails.title || !eventDetails.startDate || !eventDetails.startTime) {
        return res.status(400).json({ error: 'Missing required fields in eventDetails' });
    }

    try {
        // Constructing DateTime strings
        const startDateTime = `${eventDetails.startDate}T${eventDetails.startTime}:00Z`;
        const endDateTime = eventDetails.endDate && eventDetails.endTime 
            ? `${eventDetails.endDate}T${eventDetails.endTime}:00Z` 
            : `${eventDetails.startDate}T${eventDetails.startTime}:00Z`; // Default to same time if end time is missing

        const eventPayload = {
            summary: eventDetails.title,
            location: eventDetails.locationOrMeeting || '',
            description: eventDetails.description || '',
            startDateTime: startDateTime,
            endDateTime: endDateTime,
            timeZone: 'UTC', // Adjust time zone as necessary
            attendees: [] // Add attendee processing if needed
        };

        const event = await addEvent(userId, eventPayload);
        res.status(201).json({ message: 'Event created successfully', event });
    } catch (error) {
        res.status(500).json({ error: 'Failed to create event', details: error.message });
    }
});
/**
 * Route: DELETE /remove-event
 * Removes an event from a user's Google Calendar.
 */
router.delete('/remove-event', async (req, res) => {
    const { userId, eventId } = req.body;

    if (!userId || !eventId) {
        return res.status(400).json({ error: 'Missing required userId or eventId' });
    }

    try {
        const response = await removeEvent(userId, eventId);
        res.status(200).json(response);
    } catch (error) {
        res.status(500).json({ error: 'Failed to delete event', details: error.message });
    }
});


module.exports = router;
