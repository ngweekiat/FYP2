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

            const { credentials } = await oAuth2Client.refreshAccessToken();

            // Update Firestore with the new tokens
            await userRef.update({
                accessToken: credentials.access_token,
                tokenExpiry: Date.now() + (credentials.expiry_date || 3600 * 1000)
            });

            return credentials.access_token;
        }

        return userData.accessToken;
    } catch (error) {
        console.error('Error retrieving user token:', error.message);
        throw new Error('Failed to retrieve user token');
    }
}


async function upsertEvent(userId, eventId, eventDetails) {
    try {
        const accessToken = await getUserToken(userId);
        oAuth2Client.setCredentials({ access_token: accessToken });

        const calendar = google.calendar({ version: 'v3', auth: oAuth2Client });

        console.log(`Attempting to upsert event: ${eventId} for user ${userId}`);

        const event = {
            summary: eventDetails.summary,
            location: eventDetails.location || '',
            description: eventDetails.description || '',
            start: {
                dateTime: eventDetails.startDateTime,
                timeZone: 'Asia/Singapore',
            },
            end: {
                dateTime: eventDetails.endDateTime,
                timeZone: 'Asia/Singapore',
            },
            attendees: eventDetails.attendees || [],
        };

        try {
            // Attempt to update existing event
            const response = await calendar.events.update({
                calendarId: 'primary',
                eventId: eventId, // Ensure eventId is correctly passed
                resource: event,
            });

            // Log the response from Google
            console.log(`Event updated successfully: ${response.data.id}`);
            console.log('Response from Google Calendar API:', response.data);
            return response.data;
        } catch (error) {
            console.error(`Failed to update event: ${eventId} - Error Code: ${error.code}`);

            if (error.code === 404) {
                // If event not found, create it using the same eventId
                console.log(`Event not found (404). Creating new event for user: ${userId}`);

                const newEventResponse = await calendar.events.insert({
                    calendarId: 'primary',
                    resource: {
                        ...event,
                        id: eventId // Ensure new event uses the same eventId
                    },
                });

                // Log the response from Google when a new event is created
                console.log('New event created:', newEventResponse.data.id);
                console.log('Response from Google Calendar API:', newEventResponse.data);
                return newEventResponse.data;
            }

            throw error;
        }
    } catch (error) {
        console.error('Error in upsertEvent:', error.message);
        throw new Error('Failed to add/update Google Calendar event.');
    }
}


/**
 * Deletes an event from a user's Google Calendar.
 */
async function deleteEvent(userId, eventId) {
    try {
        const accessToken = await getUserToken(userId);
        oAuth2Client.setCredentials({ access_token: accessToken });

        const calendar = google.calendar({ version: 'v3', auth: oAuth2Client });

        console.log(`Attempting to delete event: ${eventId} for user ${userId}`);

        await calendar.events.delete({
            calendarId: 'primary',
            eventId: eventId,
        });

        console.log(`Event deleted successfully for user: ${userId}`);
        return { success: true, message: `Event ${eventId} deleted successfully.` };
    } catch (error) {
        console.error(`Failed to delete event: ${eventId} - Error Code: ${error.code}`);
        
        if (error.code === 404) {
            return { success: false, message: `Event ${eventId} not found.` };
        }
        
        throw error;
    }
}



module.exports = { upsertEvent, deleteEvent};

