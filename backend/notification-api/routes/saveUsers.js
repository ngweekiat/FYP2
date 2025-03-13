const express = require('express');
const router = express.Router();
const db = require('../config/db'); // Import Firebase db instance from db.js

/**
 * Validate the user payload.
 * @param {Object} payload - The user payload.
 * @returns {Object|null} - Returns an error object if validation fails, otherwise null.
 */
function validateUserPayload(payload) {
    const requiredFields = ['userId', 'email', 'idToken'];

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
 * POST: Save or update user authentication details in Firestore.
 */
router.post('/save-user', async (req, res) => {
    const validationError = validateUserPayload(req.body);

    if (validationError) {
        return res.status(400).json({
            message: 'Validation Error',
            error: validationError.error,
        });
    }

    const { userId, email, displayName = "", idToken, authCode } = req.body;
    
    try {
        const userRef = db.collection('users').doc(userId);
        const userSnapshot = await userRef.get();

        if (userSnapshot.exists) {
            console.log(`User ${userId} already exists, updating data.`);
        } else {
            console.log(`Saving new user: ${userId}`);
        }

        await userRef.set(
            {
                email,
                displayName: displayName || "Unknown",
                idToken,
                authCode,  // Store the Google Calendar OAuth auth code
                lastLogin: new Date().toISOString(),
            },
            { merge: true }
        );

        return res.status(201).json({
            message: 'User saved successfully',
            userId,
        });
    } catch (error) {
        return res.status(500).json({ message: 'Failed to save user', error: error.message });
    }
});

/**
 * GET: Retrieve user details by userId.
 */
router.get('/user/:userId', async (req, res) => {
    const { userId } = req.params;

    try {
        const userSnapshot = await db.collection('users').doc(userId).get();
        
        if (!userSnapshot.exists) {
            return res.status(404).json({ message: 'User not found' });
        }

        return res.status(200).json({
            message: 'User retrieved successfully',
            user: userSnapshot.data(),
        });
    } catch (error) {
        console.error('Error retrieving user:', error);
        return res.status(500).json({ message: 'Failed to retrieve user', error: error.message });
    }
});

/**
 * DELETE: Remove user details from Firestore.
 */
router.delete('/user/:userId', async (req, res) => {
    const { userId } = req.params;

    try {
        await db.collection('users').doc(userId).delete();
        return res.status(200).json({ message: 'User deleted successfully' });
    } catch (error) {
        console.error('Error deleting user:', error);
        return res.status(500).json({ message: 'Failed to delete user', error: error.message });
    }
});

module.exports = router;
