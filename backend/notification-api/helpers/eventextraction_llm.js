const { OpenAI } = require("openai");
const path = require("path");
const openaiCredentials = require(path.join(__dirname, "../config/credentials/openai-credentials.json"));

const openai = new OpenAI({
  apiKey: openaiCredentials.apiKey, // Load the API key from the JSON file
});

/**
 * Extracts event details from notification text using OpenAI API.
 * @param {string} notificationText - The notification text to process.
 * @returns {Promise<Object>} - Extracted event details as an object.
 */
async function extractEventDetails(notificationText) {
  try {
    // Log the request payload before sending it to OpenAI
    const requestPayload = {
      model: "gpt-4o",
      messages: [
        {
          role: "system",
          content: "You are a helpful assistant that extracts calendar event details. Always respond **only** in valid JSON format without any extra text.",
        },
        {
          role: "user",
          content: `Extract a calendar event from the following text: "${notificationText}". 
          Respond **only** with a valid JSON object that follows this structure: 
          {
            "title": "Event Title",
            "description": "Optional event description",
            "all_day_event": false, 
            "start_date": "YYYY-MM-DD", 
            "start_time": "HH:MM", 
            "end_date": "YYYY-MM-DD", 
            "end_time": "HH:MM"
          }.
          If certain fields are missing, use empty strings ("") but **do not return additional text**.
          `,
        },
      ],
      response_format: { type: "json_object" }, // Ensure OpenAI enforces JSON response
    };

    console.log("🟢 [DEBUG] Sending request to OpenAI:", JSON.stringify(requestPayload, null, 2));

    // Send request to OpenAI
    const response = await openai.chat.completions.create(requestPayload);

    console.log("🟡 [DEBUG] OpenAI Response:", JSON.stringify(response, null, 2)); // Log full response

    // Extract response content
    const extractedDetails = response.choices[0]?.message?.content.trim();

    if (!extractedDetails) {
      throw new Error("OpenAI returned an empty response");
    }

    console.log("✅ [DEBUG] Extracted JSON String:", extractedDetails);

    // Parse JSON response
    return JSON.parse(extractedDetails);
  } catch (error) {
    console.error("🚨 [ERROR] Error extracting event details:", error);
    throw error;
  }
}



module.exports = { extractEventDetails };
