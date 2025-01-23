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
    const response = await openai.chat.completions.create({
      model: "gpt-4",
      messages: [
        {
          role: "system",
          content: "You are a helpful assistant for extracting calendar events.",
        },
        {
          role: "user",
          content: `
          Extract a calendar event from the following text. 
          Identify the event title, description, whether it is an all-day event, start date, start time, end date, and end time. 
          Always provide the result in **valid JSON** format with the following fields: 
          {
            "title": "",
            "description": "",
            "all_day_event": false, 
            "start_date": "", 
            "start_time": "", 
            "end_date": "", 
            "end_time": ""
          }. 
          If specific details are not found, populate the fields with empty strings or set "all_day_event" to false by default. 
          Text: "${notificationText}"
        `,
        },
      ],
    });

    console.log('OpenAI Response:', JSON.stringify(response, null, 2)); // Pretty-print full response
    console.log('Message Content:', response.choices[0]?.message?.content); // Log the content directly

    const extractedDetails = response.choices[0].message.content.trim();

    try {
      return JSON.parse(extractedDetails);
    } catch (parseError) {
      console.error('Error parsing JSON:', extractedDetails); // Log the problematic response
      throw new Error("Failed to parse JSON response from OpenAI");
    }
  } catch (error) {
    console.error("Error extracting event details:", error);
    throw error;
  }
}

module.exports = { extractEventDetails };
