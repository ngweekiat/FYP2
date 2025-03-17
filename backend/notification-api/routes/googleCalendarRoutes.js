const admin = require('firebase-admin'); // Import Firebase Admin SDK
const express = require('express');
const router = express.Router();
const { upsertEvent } = require('../config/googleCalendar');
const { deleteEvent } = require('../config/googleCalendar');
const moment = require('moment-timezone');

/**
 * Route: PUT /upsert-event
 * Creates or updates an event for all authenticated users in Google Calendar.
 */
router.put('/upsert-event', async (req, res) => {
    const { eventId, eventDetails } = req.body;

    // ✅ Log the received request payload
    console.log(`Received upsert request:`);
    console.log(`eventId: ${eventId}`);
    console.log(`eventDetails: ${JSON.stringify(eventDetails, null, 2)}`);

    if (!eventDetails || !eventDetails.title || !eventDetails.startDate || !eventDetails.startTime) {
        return res.status(400).json({ error: 'Missing required fields in eventDetails' });
    }

    try {
        // Fetch the list of authenticated users
        const listUsersResult = await admin.auth().listUsers(1000); // Fetch first 1000 users
        const authenticatedUsers = listUsersResult.users.map(user => user.uid);

        // Construct DateTime strings with correct timezone
        const startDateTime = moment.tz(
            `${eventDetails.startDate} ${eventDetails.startTime}`,
            'YYYY-MM-DD HH:mm',
            'Asia/Singapore'
        ).toISOString();

        const endDateTime = eventDetails.endDate && eventDetails.endTime
            ? moment.tz(
                `${eventDetails.endDate} ${eventDetails.endTime}`,
                'YYYY-MM-DD HH:mm',
                'Asia/Singapore'
            ).toISOString()
            : moment(startDateTime).add(1, 'hour').toISOString(); // Default to 1-hour duration if end time is missing

        const eventPayload = {
            summary: eventDetails.title,
            location: eventDetails.locationOrMeeting || '',
            description: eventDetails.description || '',
            startDateTime,
            endDateTime,
            attendees: eventDetails.attendees || [],
        };

        // Upsert event for each authenticated user
        let successCount = 0;
        for (const userId of authenticatedUsers) {
            await upsertEvent(userId, eventId, eventPayload);
            successCount++;
        }

        res.status(200).json({
            message: `Event added/updated successfully for ${successCount} users`
        });

    } catch (error) {
        console.error('Error in upsert-event:', error);
        res.status(500).json({ error: 'Failed to add/update event for all users', details: error.message });
    }
});






/**
 * Route: DELETE /delete-event
 * Deletes an event from all authenticated users' Google Calendars.
 */
router.delete('/delete-event', async (req, res) => {
    console.log('Received DELETE request:', req.body); // ✅ Log request body

    const { eventId } = req.body;

    if (!eventId) {
        return res.status(400).json({ error: 'Missing required eventId' });
    }

    try {
        // Fetch authenticated users
        const listUsersResult = await admin.auth().listUsers(1000);
        const authenticatedUsers = listUsersResult.users.map(user => user.uid);

        let successCount = 0;
        let notFoundCount = 0;

        for (const userId of authenticatedUsers) {
            const response = await deleteEvent(userId, eventId);
            if (response.success) {
                successCount++;
            } else {
                notFoundCount++;
            }
        }

        res.status(200).json({
            message: `Event deleted for ${successCount} users. Not found for ${notFoundCount} users.`,
        });

    } catch (error) {
        console.error('Error in delete-event:', error);
        res.status(500).json({ error: 'Failed to delete event for all users', details: error.message });
    }
});


module.exports = router;
