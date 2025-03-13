const { google } = require('googleapis');
const db = require('../config/db'); // Firebase Firestore instance

const oAuth2Client = new google.auth.OAuth2(
    process.env.GOOGLE_CLIENT_ID,
    process.env.GOOGLE_CLIENT_SECRET,
    process.env.GOOGLE_REDIRECT_URI
);

/**
 * Retrieves the user's access token, refreshing if necessary.
 */
async function getUserToken(userId) {
    try {
        const userRef = db.collection('users').doc(userId);
        const userSnapshot = await userRef.get();

        if (!userSnapshot.exists) {
            throw new Error('User not found in Firestore');
        }

        const userData = userSnapshot.data();

        if (!userData.refreshToken) {
            throw new Error('No refresh token found for this user.');
        }

        // Check if access token is expired
        if (Date.now() > userData.tokenExpiry) {
            console.log(`Refreshing token for user: ${userId}`);
            
            const { tokens } = await oAuth2Client.refreshToken(userData.refreshToken);

            // Update Firestore with the new tokens
            await userRef.update({
                accessToken: tokens.access_token,
                tokenExpiry: Date.now() + (tokens.expiry_date || 3600 * 1000)
            });

            return tokens.access_token;
        }

        return userData.accessToken;
    } catch (error) {
        console.error('Error retrieving user token:', error.message);
        throw new Error('Failed to retrieve user token');
    }
}

/**
 * Adds an event to the user's Google Calendar.
 */
async function addEvent(userId, eventDetails) {
    try {
        const accessToken = await getUserToken(userId);
        oAuth2Client.setCredentials({ access_token: accessToken });

        const calendar = google.calendar({ version: 'v3', auth: oAuth2Client });

        const event = {
            summary: eventDetails.summary,
            location: eventDetails.location || '',
            description: eventDetails.description || '',
            start: {
                dateTime: eventDetails.startDateTime,
                timeZone: eventDetails.timeZone || 'UTC',
            },
            end: {
                dateTime: eventDetails.endDateTime,
                timeZone: eventDetails.timeZone || 'UTC',
            },
            attendees: eventDetails.attendees || [],
        };

        const response = await calendar.events.insert({
            calendarId: 'primary',
            resource: event,
        });

        console.log('Event created:', response.data);
        return response.data;
    } catch (error) {
        console.error('Error creating event:', error.message);
        throw new Error('Failed to create Google Calendar event.');
    }
}

module.exports = { addEvent };
