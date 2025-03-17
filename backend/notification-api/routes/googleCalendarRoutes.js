const admin = require('firebase-admin'); // Import Firebase Admin SDK
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
    const { eventDetails } = req.body;

    if (!eventDetails || !eventDetails.id || !eventDetails.title || !eventDetails.startDate || !eventDetails.startTime) {
        return res.status(400).json({ error: 'Missing required fields in eventDetails' });
    }

    try {
        // Fetch the list of authenticated users
        const listUsersResult = await admin.auth().listUsers(1000); // Fetch first 1000 users
        const authenticatedUsers = listUsersResult.users.map(user => user.uid);

        // Constructing DateTime strings
        const startDateTime = `${eventDetails.startDate}T${eventDetails.startTime}:00Z`;
        const endDateTime = eventDetails.endDate && eventDetails.endTime
            ? `${eventDetails.endDate}T${eventDetails.endTime}:00Z`
            : `${eventDetails.startDate}T${eventDetails.startTime}:00Z`;

        const eventPayload = {
            summary: eventDetails.title,
            location: eventDetails.locationOrMeeting || '',
            description: eventDetails.description || '',
            startDateTime: startDateTime,
            endDateTime: endDateTime,
            timeZone: 'UTC',
            attendees: []
        };

        // Add event for each authenticated user
        let successCount = 0;
        for (const userId of authenticatedUsers) {
            await addEvent(userId, eventPayload);
            successCount++;
        }

        res.status(201).json({ 
            message: `Event created successfully for ${successCount} users` 
        });

    } catch (error) {
        console.error('Error in add-event:', error);
        res.status(500).json({ error: 'Failed to create event for all users', details: error.message });
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
