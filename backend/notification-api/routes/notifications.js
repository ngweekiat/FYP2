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

    // Ignore group summary notifications
    if (req.body.isGroupSummary) {
        console.log(`Group summary notification ignored: ${req.body.key}`);
        return res.status(200).json({
            message: 'Group summary notification ignored',
            notification: req.body,
        });
    }

    const notification = {
        id: notifications.length + 1,
        ...req.body,
    };

    try {
        // Check if a notification with the same timestamp already exists in Firestore
        const existingSnapshot = await db
            .collection('notifications')
            .where('timestamp', '==', notification.timestamp)
            .get();

        if (!existingSnapshot.empty) {
            console.warn(`Notification with timestamp ${notification.timestamp} already exists.`);
            return res.status(409).json({
                message: `Notification with timestamp ${notification.timestamp} already exists`,
                existingNotification: existingSnapshot.docs[0].data(),
            });
        }

        // Store the notification in Firestore if it doesn't already exist
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

        // Check if the eventDetails object contains meaningful data
        const isEventValid = eventDetails.title || eventDetails.start_date || eventDetails.start_time;

        if (!isEventValid) {
            // If the event does not contain enough meaningful details, do not save it
            console.warn('No valid event details found in notification:', eventDetails);
            return res.status(400).json({
                message: 'No valid event details extracted from the notification',
                eventDetails,
            });
        }

        // Save extracted event to a 'calendar_events' collection
        await db.collection('calendar_events').doc(doc.id).set(eventDetails);

        res.status(200).json({
            message: 'Calendar event extracted and saved successfully',
            eventDetails,
        });
    } catch (error) {
        console.error('Error extracting calendar event:', error);
        res.status(500).json({ message: 'Failed to extract calendar event', error: error.message });
    }
});





/**
 * GET: Retrieve paginated notifications.
 */
router.get('/', async (req, res) => {
    const limit = parseInt(req.query.limit) || 20; // Default limit to 20 if not specified
    const startAfter = req.query.startAfter || null; // Start point for pagination

    try {
        let query = db.collection('notifications').orderBy('timestamp', 'desc').limit(limit);

        if (startAfter) {
            const snapshot = await db.collection('notifications').doc(startAfter).get();
            if (snapshot.exists) {
                query = query.startAfter(snapshot);
            } else {
                return res.status(400).json({ message: 'Invalid startAfter value' });
            }
        }

        const snapshot = await query.get();

        const notifications = snapshot.docs.map(doc => ({
            id: doc.id,
            ...doc.data(),
        }));

        const lastVisible = snapshot.docs[snapshot.docs.length - 1]; // Get the last document

        res.status(200).json({
            message: 'Notifications retrieved successfully',
            notifications,
            lastVisible: lastVisible ? lastVisible.id : null, // Return the last document ID for next fetch
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
















