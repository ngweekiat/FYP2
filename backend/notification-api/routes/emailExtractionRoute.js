const express = require("express");
const router = express.Router();
const { email_extractEventDetails } = require("../helpers/eventextraction_llm");

/**
 * API Route: Extract event details from an email notification.
 */
router.post("/email_extractEventDetails", async (req, res) => {
    try {
        const { notificationText, receivedAtTimestamp } = req.body;

        if (!notificationText || !receivedAtTimestamp) {
            return res.status(400).json({ error: "Missing required parameters" });
        }

        const events = await email_extractEventDetails(notificationText, receivedAtTimestamp);
        
        res.status(200).json(events);
    } catch (error) {
        console.error("Error in /email_extractEventDetails:", error);
        res.status(500).json({ error: "Failed to extract event details" });
    }
});

module.exports = router;
