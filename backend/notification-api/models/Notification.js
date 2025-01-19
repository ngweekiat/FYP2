const express = require('express');
const router = express.Router();

// Route to handle incoming notifications
router.post('/', (req, res) => {
    const notificationData = req.body;

    // Log the received notification data
    console.log('Received Notification:', notificationData);

    // Respond back with the received data for verification
    res.status(200).json({
        message: 'Notification received successfully',
        data: notificationData,
    });
});

module.exports = router;
