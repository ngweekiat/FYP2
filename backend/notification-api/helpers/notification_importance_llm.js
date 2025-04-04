const { OpenAI } = require("openai");
const path = require("path");
const openaiCredentials = require(path.join(__dirname, "../config/credentials/openai-credentials.json"));

const openai = new OpenAI({
  apiKey: openaiCredentials.apiKey, // Load the API key from the JSON file
});

/**
 * Determines if a notification contains an event.
 * @param {string} notificationText - The notification text to analyze.
 * @returns {Promise<number>} - Returns 1 if an event is detected, otherwise 0.
 */
async function detectEventInNotification(notificationText) {
  try {
    // Log the request payload before sending it to OpenAI
    const requestPayload = {
      model: "gpt-4o",
      messages: [
        {
          role: "system",
          content: "You are an AI that determines if a notification contains an event that can be added to a calendar.\n" +
                   "Respond **only** with '1' if an event is detected and '0' if not. **No extra text, no explanations, no formatting.**\n" +
                   "An event must have a clear reference to a **date, time, or schedule** to be considered valid."
        },
        {
          role: "user",
          content: `Analyze the following notification:\n\n"${notificationText}"\n\n` +
                   "Does this contain an event that can be added to a calendar?\n" +
                   "Respond **only** with '1' (yes) or '0' (no). **Do not include any other text.**"
        },
      ],
      max_tokens: 5, // Ensures no unnecessary output
    };

    console.log("🟢 [DEBUG] Sending request to OpenAI:", JSON.stringify(requestPayload, null, 2));

    // Send request to OpenAI
    const response = await openai.chat.completions.create(requestPayload);

    console.log("🟡 [DEBUG] OpenAI Response:", JSON.stringify(response, null, 2)); // Log full response

    // Extract response content
    const extractedResponse = response.choices[0]?.message?.content.trim();

    if (extractedResponse !== "0" && extractedResponse !== "1") {
      throw new Error("OpenAI returned an unexpected response: " + extractedResponse);
    }

    console.log("✅ [DEBUG] Extracted Response:", extractedResponse);

    return parseInt(extractedResponse, 10);
  } catch (error) {
    console.error("🚨 [ERROR] Error detecting event in notification:", error);
    throw error;
  }
}

module.exports = { detectEventInNotification };
