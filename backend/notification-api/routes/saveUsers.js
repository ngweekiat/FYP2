const express = require('express');
const router = express.Router();
const { google } = require('googleapis');
const db = require('../config/db'); // Firebase Firestore instance

// Set up OAuth2 Client
const oAuth2Client = new google.auth.OAuth2(
    process.env.GOOGLE_CLIENT_ID,
    process.env.GOOGLE_CLIENT_SECRET,
    process.env.GOOGLE_REDIRECT_URI
);

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

    const { uid, email, displayName = "", authCode } = req.body;

    try {
        // Exchange authCode for access & refresh tokens
        const { tokens } = await oAuth2Client.getToken(authCode);

        // Store tokens in Firestore
        const userRef = db.collection('users').doc(uid);
        await userRef.set(
            {
                uid,
                email,
                displayName: displayName || "Unknown",
                accessToken: tokens.access_token,
                refreshToken: tokens.refresh_token, // ✅ Store refresh token
                tokenExpiry: Date.now() + (tokens.expiry_date || 3600 * 1000), // Store expiry
                lastLogin: new Date().toISOString(),
            },
            { merge: true }
        );

        return res.status(201).json({
            message: 'User saved successfully',
            uid,
            accessToken: tokens.access_token
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
