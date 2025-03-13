require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const morgan = require('morgan'); // For logging requests
const helmet = require('helmet'); // For security headers
const notificationRoutes = require('./routes/notifications'); // Notification routes
const googleCalendarRoutes = require('./routes/googleCalendarRoutes'); // Google Calendar routes
const saveUserRoutes = require('./routes/saveUsers');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors()); // Enable CORS for all origins
app.use(bodyParser.json()); // Parse incoming JSON requests
app.use(morgan('dev')); // Log requests to the console
app.use(helmet()); // Secure app with various HTTP headers

// Routes
app.use('/api/notifications', notificationRoutes); // Notification-related routes
app.use('/api/google-calendar', googleCalendarRoutes); // Google Calendar integration routes
app.use('/api/users', saveUserRoutes);

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
