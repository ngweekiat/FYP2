// notifications.js
const express = require('express');
const router = express.Router();
const db = require('../config/db'); // Import the Firebase db instance from db.js
const ExcelJS = require('exceljs'); // Import ExcelJS for Excel file manipulation
const fs = require('fs'); // Import fs for file system operations

// Mock storage for notifications (replace with database later)
let notifications = [];
let failedNotifications = [];

const EXCEL_FILE_PATH = './notifications.xlsx';

/**
 * Validate the notification payload.
 * @param {Object} payload - The notification payload.
 * @returns {Object|null} - Returns an error object if validation fails, otherwise null.
 */
function validateNotificationPayload(payload) {
    const requiredFields = ['packageName', 'appName', 'title', 'timestamp'];

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
 * Export the Firebase database to an Excel file.
 */
async function exportToExcel() {
    try {
        const snapshot = await db.collection('notifications').get();
        const notifications = snapshot.docs.map(doc => doc.data());

        const workbook = new ExcelJS.Workbook();
        const worksheet = workbook.addWorksheet('Notifications');

        // Define columns
        worksheet.columns = [
            { header: 'ID', key: 'id', width: 10 },
            { header: 'Package Name', key: 'packageName', width: 30 },
            { header: 'App Name', key: 'appName', width: 30 },
            { header: 'Title', key: 'title', width: 30 },
            { header: 'Text', key: 'text', width: 30 },
            { header: 'Sub Text', key: 'subText', width: 30 },
            { header: 'Info Text', key: 'infoText', width: 30 },
            { header: 'Summary Text', key: 'summaryText', width: 30 },
            { header: 'Big Text', key: 'bigText', width: 30 },
            { header: 'Category', key: 'category', width: 20 },
            { header: 'Show When', key: 'showWhen', width: 15 },
            { header: 'Channel ID', key: 'channelId', width: 30 },
            { header: 'People', key: 'people', width: 50 },
            { header: 'Template', key: 'template', width: 30 },
            { header: 'Remote Input History', key: 'remoteInputHistory', width: 50 },
            { header: 'Timestamp', key: 'timestamp', width: 30 },
            { header: 'Tag', key: 'tag', width: 20 },
            { header: 'Key', key: 'key', width: 20 },
            { header: 'Group Key', key: 'groupKey', width: 30 },
            { header: 'Override Group Key', key: 'overrideGroupKey', width: 30 },
            { header: 'Group', key: 'group', width: 20 },
            { header: 'Is Ongoing', key: 'isOngoing', width: 15 },
            { header: 'Is Clearable', key: 'isClearable', width: 15 },
            { header: 'User Handle', key: 'userHandle', width: 30 },
            { header: 'Visibility', key: 'visibility', width: 15 },
            { header: 'Priority', key: 'priority', width: 15 },
            { header: 'Flags', key: 'flags', width: 15 },
            { header: 'Color', key: 'color', width: 15 },
            { header: 'Sound', key: 'sound', width: 30 },
            { header: 'Vibrate', key: 'vibrate', width: 30 },
            { header: 'Audio Stream Type', key: 'audioStreamType', width: 20 },
            { header: 'Content View', key: 'contentView', width: 30 },
            { header: 'Big Content View', key: 'bigContentView', width: 30 },
            { header: 'Is Group Summary', key: 'isGroupSummary', width: 15 },
            { header: 'Actions', key: 'actions', width: 50 },
        ];

        // Add rows
        notifications.forEach(notification => {
            worksheet.addRow(notification);
        });

        // Save the Excel file
        await workbook.xlsx.writeFile(EXCEL_FILE_PATH);
        console.log('Excel file updated successfully');
    } catch (error) {
        console.error('Error exporting to Excel:', error);
    }
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

        // Export to Excel
        await exportToExcel();

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
