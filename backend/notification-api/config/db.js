// db.js
const admin = require('firebase-admin');

// Correct path to your Firebase service account key
const serviceAccount = require('C:/Users/ngwee/Downloads/Desktop/FYP/backend/notification-api/fypeventcalendar-firebase-adminsdk-fbsvc-5b225452c9.json');

// Initialize Firebase only once
if (!admin.apps.length) {
    admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
        databaseURL: 'https://fypeventcalendar-default-rtdb.firebaseio.com/' // Firebase Realtime Database URL (or omit for Firestore)
    });
} else {
    admin.app(); // Use the existing app if already initialized
}

const db = admin.firestore(); // Use Firestore (or admin.database() for Realtime Database)

module.exports = db;
