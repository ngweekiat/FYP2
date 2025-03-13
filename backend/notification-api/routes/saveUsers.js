const express = require('express');
const router = express.Router();
const db = require('../config/db'); // Firebase Firestore instance

/**
 * Validate the user payload.
 */
function validateUserPayload(payload) {
    const requiredFields = ['uid', 'email', 'idToken', 'authCode'];
    for (const field of requiredFields) {
        if (!payload[field]) {
            return { error: `Missing required field: ${field}` };
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

    const { uid, email, displayName = "" } = req.body;

    try {
        // Store user details in Firestore (no need to handle token here)
        const userRef = db.collection('users').doc(uid);
        await userRef.set(
            {
                uid,
                email,
                displayName: displayName || "Unknown",
                lastLogin: new Date().toISOString(),
            },
            { merge: true }
        );

        return res.status(201).json({
            message: 'User saved successfully',
            uid,
        });
    } catch (error) {
        console.error('Error saving user:', error.message);
        return res.status(500).json({ message: 'Failed to save user', error: error.message });
    }
});

/**
 * GET: Retrieve user details by UID.
 */
router.get('/user/:uid', async (req, res) => {
    const { uid } = req.params;

    try {
        const userSnapshot = await db.collection('users').doc(uid).get();
        
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
router.delete('/user/:uid', async (req, res) => {
    const { uid } = req.params;

    try {
        await db.collection('users').doc(uid).delete();
        return res.status(200).json({ message: 'User deleted successfully' });
    } catch (error) {
        console.error('Error deleting user:', error);
        return res.status(500).json({ message: 'Failed to delete user', error: error.message });
    }
});

module.exports = router;
