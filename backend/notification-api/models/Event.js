const { DataTypes } = require('sequelize');
const sequelize = require('../config/db'); // Import the database connection

const Event = sequelize.define('Event', {
    // Event summary or title
    summary: {
        type: DataTypes.STRING,
        allowNull: false, // Event must have a summary
    },
    // Event location
    location: {
        type: DataTypes.STRING,
        allowNull: true, // Location is optional
    },
    // Event description
    description: {
        type: DataTypes.TEXT,
        allowNull: true, // Description is optional
    },
    // Start time of the event
    start_time: {
        type: DataTypes.DATE,
        allowNull: false, // Start time is mandatory
    },
    // End time of the event
    end_time: {
        type: DataTypes.DATE,
        allowNull: false, // End time is mandatory
    },
    // List of attendees (stored as JSON)
    attendees: {
        type: DataTypes.JSONB,
        allowNull: true, // Optional
        defaultValue: [], // Default to an empty list if no attendees
    },
}, {
    timestamps: true, // Automatically adds createdAt and updatedAt columns
    tableName: 'events', // Define the table name explicitly
});

// Export the model for use in other parts of the application
module.exports = Event;
