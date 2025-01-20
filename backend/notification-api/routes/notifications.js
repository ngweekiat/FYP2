// notifications.js
const express = require('express');
const router = express.Router();
const db = require('../config/db'); // Import the Firebase db instance from db.js
const { extractEventDetails } = require('../helpers/eventextraction_llm');


// Mock storage for notifications (replace with database later)
let notifications = [];
let failedNotifications = [];

/**
 * Validate the notification payload.
 * @param {Object} payload - The notification payload.
 * @returns {Object|null} - Returns an error object if validation fails, otherwise null.
 */
function validateNotificationPayload(payload) {
    const requiredFields = ['packageName', 'title', 'timestamp'];

    for (const field of requiredFields) {
        if (!payload[field]) {
            return {
                error: `Missing required field: ${field}`,
                requiredFields,
            };
        }
    }
    return null;
}

/**
 * POST: Receive a new notification.
 */
router.post('/', async (req, res) => {
    const validationError = validateNotificationPayload(req.body);

    if (validationError) {
        const failedNotification = {
            ...req.body,
            id: failedNotifications.length + 1,
            error: validationError.error,
        };

        failedNotifications.push(failedNotification);

        console.error('Validation Error:', validationError.error);
        return res.status(400).json({
            message: 'Validation Error',
            error: validationError.error,
            failedNotification,
        });
    }

    const notification = {
        id: notifications.length + 1,
        ...req.body,
    };

    // Store the notification in Firestore
    try {
        const docRef = await db.collection('notifications').add(notification);
        notifications.push(notification);

        console.log('Received Notification:', notification);
        return res.status(201).json({
            message: 'Notification received',
            notification,
        });
    } catch (error) {
        console.error('Error saving notification:', error);
        return res.status(500).json({ message: 'Failed to save notification' });
    }
});


/**
 * POST: Extract event details from a notification.
 */
router.post('/extract-event', async (req, res) => {
    const { notificationId } = req.body;

    if (!notificationId) {
        return res.status(400).json({ message: 'Missing required field: notificationId' });
    }

    try {
        // Search for the notification by the `id` field
        const snapshot = await db.collection('notifications').where('id', '==', notificationId).get();

        if (snapshot.empty) {
            return res.status(404).json({ message: 'Notification not found', id: notificationId });
        }

        // Assuming `id` is unique, get the first matching document
        const doc = snapshot.docs[0];
        const notification = doc.data();
        const { title, bigText, timestamp } = notification;

        // Combine fields into a single input text for OpenAI
        const notificationText = `${title}\n${bigText}\nTimestamp: ${timestamp}`;

        // Extract calendar event details
        const eventDetails = await extractEventDetails(notificationText);

        // Save extracted event to a 'calendar_events' collection
        await db.collection('calendar_events').doc(doc.id).set(eventDetails);

        res.status(200).json({
            message: 'Calendar event extracted successfully',
            eventDetails,
        });
    } catch (error) {
        console.error('Error extracting calendar event:', error);
        res.status(500).json({ message: 'Failed to extract calendar event', error: error.message });
    }
});




/**
 * GET: Retrieve all successful notifications.
 */
router.get('/', async (req, res) => {
    try {
        const snapshot = await db.collection('notifications').get();
        const notifications = snapshot.docs.map(doc => doc.data());
        
        res.status(200).json({
            message: 'All Notifications',
            notifications,
        });
    } catch (error) {
        console.error('Error retrieving notifications:', error);
        res.status(500).json({ message: 'Failed to retrieve notifications' });
    }
});

/**
 * GET: Retrieve all failed notifications.
 */
router.get('/failed', (req, res) => {
    res.status(200).json({
        message: 'Failed Notifications',
        failedNotifications,
    });
});

module.exports = router;
















