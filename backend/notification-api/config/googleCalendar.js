const { google } = require('googleapis');
const db = require('../config/db'); // Import Firebase instance

const SCOPES = ['https://www.googleapis.com/auth/calendar'];

let oAuth2Client = new google.auth.OAuth2(
    process.env.GOOGLE_CLIENT_ID,
    process.env.GOOGLE_CLIENT_SECRET,
    process.env.GOOGLE_REDIRECT_URI
);

/**
 * Retrieve and refresh the user's Google OAuth token from Firestore.
 * @param {string} userId - The user's ID in Firestore.
 * @returns {Promise<object>} - OAuth2 tokens.
 */
async function getUserToken(userId) {
    try {
        const userRef = db.collection('users').doc(userId);
        const userSnapshot = await userRef.get();

        if (!userSnapshot.exists) {
            throw new Error('User not found in Firestore');
        }

        const userData = userSnapshot.data();
        if (!userData.authCode) {
            throw new Error('No auth token found for this user.');
        }

        // Exchange the authCode for an access token
        const { tokens } = await oAuth2Client.getToken(userData.authCode);
        oAuth2Client.setCredentials(tokens);

        return tokens;
    } catch (error) {
        console.error('Error retrieving user token:', error.message);
        throw new Error('Failed to retrieve user token');
    }
}

/**
 * Adds an event to the user's Google Calendar.
 * @param {string} userId - The user ID (to retrieve the token).
 * @param {object} eventDetails - The event details.
 * @returns {Promise<object>} - The created event.
 */
async function addEvent(userId, eventDetails) {
    try {
        const tokens = await getUserToken(userId);
        oAuth2Client.setCredentials(tokens);

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
