// notifications.js
const express = require('express');
const router = express.Router();
const db = require('../config/db'); // Import the Firebase db instance from db.js
const { extractEventDetails } = require('../helpers/eventextraction_llm');
const { detectEventInNotification } = require('../helpers/notification_importance_llm'); // Import event detection function


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
 * POST: Receive a new notification and automatically extract event details if applicable.
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
        // Check if a notification with the same id already exists in Firestore
        const existingSnapshot = await db
            .collection('notifications')
            .where('id', '==', notification.id)
            .get();
    
        if (!existingSnapshot.empty) {
            console.warn(`Notification with id ${notification.id} already exists.`);
            return res.status(409).json({
                message: `Notification with id ${notification.id} already exists`,
                existingNotification: existingSnapshot.docs[0].data(),
            });
        }

        // Store the notification in Firestore
        const docRef = await db.collection('notifications').add(notification);
        notifications.push(notification);

        console.log('Received Notification:', notification);

        // Combine text fields into one message for processing
        const notificationText = `${notification.title || ''}\n${notification.text || ''}\n${notification.bigText || ''}\nTimestamp: ${notification.timestamp || ''}`;

        // Detect if notification contains an event
        const isImportant = await detectEventInNotification(notificationText);

        // Update the notification document with the importance field
        await db.collection('notifications').doc(docRef.id).update({
            notification_importance: isImportant
        });

        if (isImportant === 1) {
            console.log('🔍 Notification contains an event, extracting details...');

            // Extract calendar event details
            const eventDetails = await extractEventDetails(notificationText, notification.timestamp);

            // Check if extracted event is meaningful
            const isEventValid = eventDetails.title || eventDetails.start_date || eventDetails.start_time;

            if (isEventValid) {
                console.log('✅ Valid event extracted:', eventDetails);

                // Save extracted event to 'calendar_events'
                await db.collection('calendar_events').doc(docRef.id).set(
                    { ...eventDetails, id: 
                        notification.id , 
                        button_status: 0 },
                    { merge: true }
                );
            } else {
                console.warn('⚠️ No valid event details found in notification:', eventDetails);
            }
        } else {
            console.log('ℹ️ Notification does not contain an important event.');
        }

        return res.status(201).json({
            message: 'Notification received and processed',
            notification,
        });
    } catch (error) {
        console.error('🚨 Error processing notification:', error);
        return res.status(500).json({ message: 'Failed to process notification' });
    }
});





/**
 * POST: Extract event details from a notification if it's important.
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
        const { title, text, bigText, timestamp } = notification;

        // Combine fields into a single input text for OpenAI
        const notificationText = `${title || ''}\n${text || ''}\n${bigText || ''}\nTimestamp: ${timestamp || ''}`;

        // Check if the notification contains an important event
        const isImportant = await detectEventInNotification(notificationText);

        // Update the notification document with the importance field
        await db.collection('notifications').doc(doc.id).update({
            notification_importance: isImportant
        });

        if (isImportant !== 1) {
            console.warn('Notification does not contain an important event:', notificationText);
            return res.status(400).json({
                message: 'Notification does not contain an important event, skipping extraction.',
                notificationId,
            });
        }

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
        await db.collection('calendar_events').doc(doc.id).set(
            { ...eventDetails, id: notificationId, button_status: 0 },
            { merge: true }
        );
        

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
 * GET: Retrieve a calendar event by ID.
 */
router.get('/calendar_events/:id', async (req, res) => {
    const eventId = req.params.id;

    try {
        // Search for the event document in Firestore by ID
        const snapshot = await db.collection('calendar_events').where('id', '==', eventId).get();

        if (snapshot.empty) {
            return res.status(404).json({ message: 'Calendar event not found', id: eventId });
        }

        // Assuming ID is unique, return the first document found
        const doc = snapshot.docs[0];
        const eventData = doc.data();

        res.status(200).json({
            message: 'Calendar event retrieved successfully',
            event: eventData,
        });
    } catch (error) {
        console.error('Error retrieving calendar event:', error);
        res.status(500).json({ message: 'Failed to retrieve calendar event', error: error.message });
    }
});

/**
 * GET: Fetch events for the specific year and month
 */
router.get('/calendar_events', async (req, res) => {
    const { year, month } = req.query;

    if (!year || !month) {
        return res.status(400).json({ message: 'Missing required parameters: year and month' });
    }

    try {
        const snapshot = await db.collection('calendar_events')
            .where('start_date', '>=', `${year}-${month.padStart(2, '0')}-01`)
            .where('start_date', '<=', `${year}-${month.padStart(2, '0')}-31`)
            .where('button_status', '==', 1) // ✅ Only fetch saved events
            .get();

        if (snapshot.empty) {
            return res.status(404).json({ message: 'No saved calendar events found for the selected month' });
        }

        const events = snapshot.docs.map(doc => doc.data());

        res.status(200).json(events);
    } catch (error) {
        console.error('Error retrieving calendar events:', error);
        res.status(500).json({ message: 'Failed to retrieve calendar events', error: error.message });
    }
});

/**
 * PATCH: Update notification importance by ID.
 */
router.patch('/:id/updateImportance', async (req, res) => {
    const notificationId = req.params.id;
    const { notification_importance } = req.body;

    if (typeof notification_importance !== 'number') {
        return res.status(400).json({ message: 'Invalid or missing notification_importance' });
    }

    try {
        // Find the notification document in Firestore
        const snapshot = await db.collection('notifications').where('id', '==', notificationId).get();

        if (snapshot.empty) {
            return res.status(404).json({ message: 'Notification not found', id: notificationId });
        }

        // Update the notification importance
        const doc = snapshot.docs[0];
        await db.collection('notifications').doc(doc.id).update({ notification_importance });

        res.status(200).json({
            message: 'Notification importance updated successfully',
            notificationId,
            notification_importance,
        });
    } catch (error) {
        console.error('Error updating notification importance:', error);
        res.status(500).json({ message: 'Failed to update notification importance', error: error.message });
    }
});


/**
 * PATCH: Update an existing calendar event by ID.
 */
router.patch('/calendar_events/:id', async (req, res) => {
    const eventId = req.params.id;
    let updatedEventData = req.body; // Data to update

    try {
        // Find the existing event document in Firestore
        const snapshot = await db.collection('calendar_events').where('id', '==', eventId).get();

        if (snapshot.empty) {
            return res.status(404).json({ message: 'Calendar event not found', id: eventId });
        }

        // Get the first matching document (assuming ID is unique)
        const doc = snapshot.docs[0];
        const existingEvent = doc.data();

        // Use existing values if the new values are undefined
        const startDate = updatedEventData.start_date ? new Date(updatedEventData.start_date) : new Date(existingEvent.start_date);
        let endDate = updatedEventData.end_date ? new Date(updatedEventData.end_date) : startDate;

        let startTime = updatedEventData.start_time || existingEvent.start_time || "00:00";
        let endTime = updatedEventData.end_time || existingEvent.end_time;

        if (!updatedEventData.end_date || !updatedEventData.end_time) {
            // Parse start time (HH:MM format)
            const [startHour, startMinute] = startTime.split(":").map(Number);

            // Add one hour to the start time
            let adjustedEndTime = new Date(startDate);
            adjustedEndTime.setHours(startHour + 1, startMinute);

            endDate = adjustedEndTime;
            endTime = `${adjustedEndTime.getHours().toString().padStart(2, '0')}:${adjustedEndTime.getMinutes().toString().padStart(2, '0')}`;
        }

        // Prepare updated event data
        const updatedEvent = {
            ...existingEvent,
            ...updatedEventData,
            start_date: startDate.toISOString().split("T")[0],
            end_date: endDate.toISOString().split("T")[0],
            start_time: startTime,
            end_time: endTime,
        };

        // Update the event document in Firestore
        await db.collection('calendar_events').doc(doc.id).update(updatedEvent);

        res.status(200).json({
            message: 'Calendar event updated successfully',
            updatedEvent,
        });
    } catch (error) {
        console.error('Error updating calendar event:', error);
        res.status(500).json({ message: 'Failed to update calendar event', error: error.message });
    }
});



/**
 * GET: Fetch all calendar events.
 */
router.get('/calendar_events_all', async (req, res) => {
    try {
        const snapshot = await db.collection('calendar_events').get();

        if (snapshot.empty) {
            return res.status(404).json({ message: 'No calendar events found' });
        }

        const events = snapshot.docs.map(doc => doc.data());

        res.status(200).json({
            message: 'All calendar events retrieved successfully',
            events,
        });
    } catch (error) {
        console.error('Error retrieving all calendar events:', error);
        res.status(500).json({ message: 'Failed to retrieve calendar events', error: error.message });
    }
});




/**
 * GET: Retrieve paginated notifications.
 */
router.get('/', async (req, res) => {
    const limit = parseInt(req.query.limit) || 20; // Default limit to 20 if not specified
    const startAfter = req.query.startAfter || null; // Start point for pagination

    try {
        let query = db.collection('notifications')
            .where('category', 'in', ['email', 'msg'])
            .orderBy('timestamp', 'desc')
            .limit(limit);
            
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


// Burner code to download to excel
// call http://localhost:3000/notifications/export
const XLSX = require('xlsx'); // Install the 'xlsx' package: npm install xlsx

/**
 * GET: Download the entire notification collection as an Excel file.
 */
router.get('/export', async (req, res) => {
    try {
        // Fetch all notifications from the 'notifications' collection
        const snapshot = await db.collection('notifications').get();

        if (snapshot.empty) {
            return res.status(404).json({ message: 'No notifications found' });
        }

        // Map the snapshot documents to an array of objects
        const notifications = snapshot.docs.map(doc => ({
            id: doc.id,
            ...doc.data(),
        }));

        // Create a worksheet from the notifications data
        const worksheet = XLSX.utils.json_to_sheet(notifications);

        // Create a new workbook and append the worksheet
        const workbook = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(workbook, worksheet, 'Notifications');

        // Write the workbook to a buffer
        const buffer = XLSX.write(workbook, { type: 'buffer', bookType: 'xlsx' });

        // Set the appropriate headers for file download
        res.setHeader('Content-Disposition', 'attachment; filename="notifications.xlsx"');
        res.setHeader('Content-Type', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');

        // Send the buffer as a response
        res.status(200).send(buffer);
    } catch (error) {
        console.error('Error exporting notifications to Excel:', error);
        res.status(500).json({ message: 'Failed to export notifications', error: error.message });
    }
});


/**
 * POST: Create or Update a calendar event.
 */
router.post('/calendar_events', async (req, res) => {
    let eventData = req.body;

    if (!eventData.id) {
        return res.status(400).json({ message: 'Missing required field: event ID' });
    }

    try {
        // Check if event with the same ID already exists
        const snapshot = await db.collection('calendar_events').where('id', '==', eventData.id).get();

        let existingEvent = null;
        let docRef = null;

        if (!snapshot.empty) {
            // Event exists, update instead of creating a new one
            docRef = snapshot.docs[0].ref;
            existingEvent = snapshot.docs[0].data();
            console.log(`Updating existing event: ${eventData.id}`);
        } else {
            // Event does not exist, create a new one
            console.log(`Creating new event: ${eventData.id}`);
            docRef = db.collection('calendar_events').doc(eventData.id);
        }

        // Use existing values if new ones are undefined
        const startDate = eventData.start_date ? new Date(eventData.start_date) : new Date(existingEvent?.start_date || Date.now());
        let endDate = eventData.end_date ? new Date(eventData.end_date) : new Date(startDate);

        let startTime = eventData.start_time || existingEvent?.start_time || "00:00";
        let endTime = eventData.end_time || existingEvent?.end_time;

        // Ensure valid date format
        if (!eventData.end_date || !eventData.end_time) {
            // Parse start time (HH:MM format)
            const [startHour, startMinute] = startTime.split(":").map(Number);

            // Add one hour to the start time
            let adjustedEndTime = new Date(startDate);
            adjustedEndTime.setHours(startHour + 1, startMinute);

            endDate = adjustedEndTime;
            endTime = `${adjustedEndTime.getHours().toString().padStart(2, '0')}:${adjustedEndTime.getMinutes().toString().padStart(2, '0')}`;
        }

        // Prepare updated or new event data
        const eventToSave = {
            id: eventData.id,
            title: eventData.title || existingEvent?.title || "",
            start_date: startDate.toISOString().split("T")[0],
            end_date: endDate.toISOString().split("T")[0],
            start_time: startTime,
            end_time: endTime,
            location: eventData.location || existingEvent?.location || "",
            description: eventData.description || existingEvent?.description || "",
            button_status: eventData.button_status ?? existingEvent?.button_status ?? 0, // Keep existing status if available
        };

        // Save the event (either create or update)
        await docRef.set(eventToSave, { merge: true });

        res.status(snapshot.empty ? 201 : 200).json({
            message: snapshot.empty ? 'Calendar event created successfully' : 'Calendar event updated successfully',
            event: eventToSave,
        });

    } catch (error) {
        console.error('🚨 Error creating/updating calendar event:', error);
        res.status(500).json({ message: 'Failed to process calendar event', error: error.message });
    }
});




module.exports = router;

