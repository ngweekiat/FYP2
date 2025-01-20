const express = require('express');
const router = express.Router();
const { initializeOAuth, getAuthUrl, saveToken, addEvent } = require('../config/googleCalendar');

// Initialize OAuth Client
try {
    initializeOAuth();
} catch (err) {
    console.error('Failed to initialize OAuth:', err.message);
}

// Step 1: Get Authentication URL
router.get('/auth', (req, res) => {
    try {
        const authUrl = getAuthUrl();
        res.redirect(authUrl);
    } catch (err) {
        console.error('Error generating auth URL:', err.message);
        res.status(500).json({ error: 'Failed to generate authentication URL' });
    }
});

// Step 2: Handle OAuth Callback
router.get('/oauth2callback', async (req, res) => {
    const code = req.query.code;
    if (!code) {
        console.error('Missing authorization code');
        return res.status(400).json({ error: 'Authorization code is required' });
    }

    try {
        const result = await saveToken(code);
        console.log('Token exchange successful:', result);
        res.send('Authorization successful! You can now add events to your calendar.');
    } catch (err) {
        console.error('Error during token exchange:', err.message);
        res.status(500).json({ error: 'Failed to exchange authorization code for tokens', details: err.message });
    }
});

// Step 3: Add Event
router.post('/add-event', async (req, res) => {
    const eventDetails = req.body;
    if (!eventDetails.summary || !eventDetails.startDateTime || !eventDetails.endDateTime) {
        console.error('Missing required event details:', eventDetails);
        return res.status(400).json({ error: 'Event summary, startDateTime, and endDateTime are required' });
    }

    try {
        const event = await addEvent(eventDetails);
        res.status(201).json({ message: 'Event created successfully', event });
    } catch (err) {
        console.error('Error adding event:', err.message);
        res.status(500).json({ error: 'Failed to create event', details: err.message });
    }
});

module.exports = router;
