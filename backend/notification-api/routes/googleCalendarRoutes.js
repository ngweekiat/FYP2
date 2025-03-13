const express = require('express');
const router = express.Router();
const { addEvent } = require('../config/googleCalendar');

/**
 * Route: POST /add-event
 * Adds an event to a user's Google Calendar.
 * Requires userId, summary, startDateTime, and endDateTime in the request body.
 */
router.post('/add-event', async (req, res) => {
    const { userId, summary, startDateTime, endDateTime, location, description, attendees, timeZone } = req.body;

    if (!userId || !summary || !startDateTime || !endDateTime) {
        return res.status(400).json({ error: 'Missing required userId, summary, startDateTime, or endDateTime' });
    }

    try {
        const eventDetails = { summary, startDateTime, endDateTime, location, description, attendees, timeZone };
        const event = await addEvent(userId, eventDetails);
        res.status(201).json({ message: 'Event created successfully', event });
    } catch (error) {
        res.status(500).json({ error: 'Failed to create event', details: error.message });
    }
});

module.exports = router;
