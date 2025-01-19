const express = require('express');
const router = express.Router();
const fs = require('fs');
const xlsx = require('xlsx');

// File path for the workbook
const filePath = './notifications_log.xlsx';

/**
 * Ensure the workbook and worksheet exist.
 */
function initializeWorkbook() {
    if (!fs.existsSync(filePath)) {
        const workbook = xlsx.utils.book_new();
        const worksheet = xlsx.utils.json_to_sheet([]); // Create an empty worksheet
        xlsx.utils.book_append_sheet(workbook, worksheet, 'Notifications');
        xlsx.writeFile(workbook, filePath);
    }
}

/**
 * Refresh the entire worksheet with current notifications.
 * @param {Array} notifications - Array of successful notifications.
 * @param {Array} failedNotifications - Array of failed notifications.
 */
function refreshWorksheet(notifications, failedNotifications) {
    initializeWorkbook();

    const workbook = xlsx.utils.book_new();

    // Combine successful and failed notifications with a status column
    const allNotifications = [
        ...notifications.map((n) => ({ ...n, status: 'Success' })),
        ...failedNotifications.map((f) => ({ ...f, status: 'Failed' })),
    ];

    // Convert the combined data to a worksheet
    const worksheet = xlsx.utils.json_to_sheet(allNotifications);

    // Append the worksheet to the workbook
    xlsx.utils.book_append_sheet(workbook, worksheet, 'Notifications');

    // Save the workbook
    xlsx.writeFile(workbook, filePath);
}

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
router.post('/', (req, res) => {
    const validationError = validateNotificationPayload(req.body);

    if (validationError) {
        const failedNotification = {
            ...req.body,
            id: failedNotifications.length + 1,
            error: validationError.error,
        };

        failedNotifications.push(failedNotification);
        refreshWorksheet(notifications, failedNotifications);

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

    notifications.push(notification);
    refreshWorksheet(notifications, failedNotifications);

    console.log('Received Notification:', notification);
    res.status(201).json({
        message: 'Notification received',
        notification,
    });
});

/**
 * GET: Retrieve all successful notifications.
 */
router.get('/', (req, res) => {
    res.status(200).json({
        message: 'All Notifications',
        notifications,
    });
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

/**
 * GET: Download the Excel file.
 */
router.get('/export/log', (req, res) => {
    if (fs.existsSync(filePath)) {
        res.download(filePath, 'notifications_log.xlsx', (err) => {
            if (err) {
                console.error('Error downloading the file:', err);
                res.status(500).json({ error: 'Error downloading the file' });
            }
        });
    } else {
        res.status(404).json({ error: 'No log file found' });
    }
});

module.exports = router;
