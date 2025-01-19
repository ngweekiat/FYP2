const { Sequelize } = require('sequelize');

const sequelize = new Sequelize('notification_calendar', 'postgres', 'password', {
    host: 'localhost',
    dialect: 'postgres',
    logging: false, // Disable query logging for cleaner output
});

sequelize
    .authenticate()
    .then(() => console.log('Database connection established successfully.'))
    .catch((err) => console.error('Unable to connect to the database:', err.message));

module.exports = sequelize;
