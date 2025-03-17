const axios = require('axios');
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

    const { uid, email, displayName = "", authCode } = req.body;

    try {
        let refreshToken = null;
        let accessToken = null;
        let tokenExpiry = null;

        // If authCode is provided, exchange it for tokens
        if (authCode) {
            const tokenResponse = await axios.post('https://oauth2.googleapis.com/token', null, {
                params: {
                    client_id: process.env.GOOGLE_CLIENT_ID,
                    client_secret: process.env.GOOGLE_CLIENT_SECRET,
                    code: authCode,
                    grant_type: 'authorization_code',
                    redirect_uri: process.env.GOOGLE_REDIRECT_URI,
                }
            });

            accessToken = tokenResponse.data.access_token;
            refreshToken = tokenResponse.data.refresh_token || null;
            tokenExpiry = Date.now() + tokenResponse.data.expires_in * 1000;
        }

        // Store user details in Firestore
        const userRef = db.collection('users').doc(uid);
        await userRef.set(
            {
                uid,
                email,
                displayName: displayName || "Unknown",
                lastLogin: new Date().toISOString(),
                accessToken,
                refreshToken, // Store refresh token
                tokenExpiry
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
