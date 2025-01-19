require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const morgan = require('morgan'); // For logging requests
const helmet = require('helmet'); // For security headers
const sequelize = require('./config/db'); // Database connection
const notificationRoutes = require('./routes/notifications'); // Notification routes
const googleCalendarRoutes = require('./routes/googleCalendarRoutes'); // Google Calendar routes

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors()); // Enable CORS for all origins
app.use(bodyParser.json()); // Parse incoming JSON requests
app.use(morgan('dev')); // Log requests to the console
app.use(helmet()); // Secure app with various HTTP headers

// Test Database Connection and Sync Models
(async () => {
    try {
        await sequelize.authenticate();
        console.log('Database connection established successfully.');

        // Sync models
        await sequelize.sync({ alter: true }); // Use { force: true } to recreate tables (WARNING: Drops existing data)
        console.log('Database models synchronized successfully.');
    } catch (err) {
        console.error('Unable to connect to the database or sync models:', err.message);
        process.exit(1); // Exit the application if the database connection fails
    }
})();

// Routes
app.use('/api/notifications', notificationRoutes); // Notification-related routes
app.use('/api/google-calendar', googleCalendarRoutes); // Google Calendar integration routes

// Root Route
app.get('/', (req, res) => {
    res.status(200).send('Notification API is running');
});

// Global Error Handler
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({
        message: 'An unexpected error occurred',
        error: process.env.NODE_ENV === 'development' ? err.message : 'Internal Server Error',
    });
});

// Start Server
app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
