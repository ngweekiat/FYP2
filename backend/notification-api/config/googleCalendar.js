const { google } = require('googleapis');
const path = require('path');
const fs = require('fs');

const SCOPES = ['https://www.googleapis.com/auth/calendar'];
const CREDENTIALS_PATH = path.join(__dirname, 'credentials', 'google-calendar-credentials.json');
const TOKEN_PATH = path.join(__dirname, 'credentials', 'google-calendar-token.json');


let oAuth2Client;

// Initialize OAuth Client
function initializeOAuth() {
    try {
        const credentials = JSON.parse(fs.readFileSync(CREDENTIALS_PATH, 'utf8'));
        const { client_id, client_secret, redirect_uris } = credentials.installed || credentials.web;
        oAuth2Client = new google.auth.OAuth2(client_id, client_secret, redirect_uris[0]);
        loadToken(); // Load saved token if it exists
    } catch (err) {
        console.error('Error initializing OAuth client:', err.message);
        throw new Error('Failed to initialize OAuth client. Ensure credentials.json is correct.');
    }
}

// Generate Auth URL
function getAuthUrl() {
    return oAuth2Client.generateAuthUrl({
        access_type: 'offline',
        scope: SCOPES,
    });
}

// Save Access Token
function saveToken(code) {
    return new Promise((resolve, reject) => {
        oAuth2Client.getToken(code, (err, token) => {
            if (err) {
                console.error('Error retrieving access token:', err.message);
                return reject(new Error('Failed to retrieve access token. Check authorization code and redirect URI.'));
            }
            try {
                fs.writeFileSync(TOKEN_PATH, JSON.stringify(token));
                oAuth2Client.setCredentials(token);
                console.log('Token successfully stored to', TOKEN_PATH);
                resolve('Token stored successfully');
            } catch (fileErr) {
                console.error('Error saving token:', fileErr.message);
                reject(new Error('Failed to save access token.'));
            }
        });
    });
}

// Load Existing Token
function loadToken() {
    if (fs.existsSync(TOKEN_PATH)) {
        try {
            const token = JSON.parse(fs.readFileSync(TOKEN_PATH, 'utf8'));
            oAuth2Client.setCredentials(token);
            console.log('Token loaded successfully');
        } catch (err) {
            console.error('Error loading token:', err.message);
        }
    } else {
        console.log('No token found. User needs to authorize.');
    }
}

// Add an Event to Google Calendar
async function addEvent(eventDetails) {
    try {
        const calendar = google.calendar({ version: 'v3', auth: oAuth2Client });

        const event = {
            summary: eventDetails.summary,
            location: eventDetails.location,
            description: eventDetails.description,
            start: {
                dateTime: eventDetails.startDateTime,
                timeZone: 'America/Los_Angeles',
            },
            end: {
                dateTime: eventDetails.endDateTime,
                timeZone: 'America/Los_Angeles',
            },
            attendees: eventDetails.attendees,
        };

        const response = await calendar.events.insert({
            calendarId: 'primary',
            resource: event,
        });

        console.log('Event created:', response.data);
        return response.data;
    } catch (err) {
        console.error('Error creating event:', err.message);
        throw new Error('Failed to create Google Calendar event.');
    }
}

module.exports = { initializeOAuth, getAuthUrl, saveToken, loadToken, addEvent };
