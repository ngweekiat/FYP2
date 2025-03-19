const { OpenAI } = require("openai");
const path = require("path");
const openaiCredentials = require(path.join(__dirname, "../config/credentials/openai-credentials.json"));
const { z } = require('zod');


const openai = new OpenAI({
  apiKey: openaiCredentials.apiKey, // Load the API key from the JSON file
});

/**
 * Extracts event details from notification text using OpenAI API.
 * @param {string} notificationText - The notification text to process.
 * @returns {Promise<Object>} - Extracted event details as an object.
 */
async function extractEventDetails(notificationText, receivedAtTimestamp) {
  const requestPayload = {
    model: "gpt-4o",
    messages: [
      {
        role: "user",
        content: `
        You are an intelligent assistant that extracts calendar events from notifications. 
        You **must** respond strictly in valid JSON matching this format:
        \`\`\`json
        {
          "title": "Event Title",
          "description": "Optional event description",
          "all_day_event": false,
          "start_date": "YYYY-MM-DD",
          "start_time": "HH:MM",
          "end_date": "YYYY-MM-DD",
          "end_time": "HH:MM"
        }
        \`\`\`

        **Notification Details:**  
        - Notification text: "${notificationText}"  
        - Received timestamp: "${receivedAtTimestamp}"  

      ### **Rules for Extracting Events:**  
      1. **Date Interpretation**  
         - Interpret relative dates/times based on the received timestamp.  
         - \"Next [weekday]\" refers to the **weekday of the following week**.  
         - \"This [weekday]\" refers to the **upcoming occurrence of that weekday**.  

      2. **Handling Missing Information**  
         - **Do NOT assume or infer missing end date or time.**  
         - If the end date or end time is not explicitly mentioned, set \"end_date\": \"\" and \"end_time\": \"\".  
         - If the event time is not given, set \"start_time\": \"\" and \"end_time\": \"\".  
         - If any detail is missing or unclear, return it as an empty string \"\" instead of making assumptions.  

      3. **All-Day Events**  
         - If explicitly stated as \"all day,\" set \"all_day_event\": true and leave \"start_time\" and \"end_time\" empty.  

      **DO NOT** include extra text or formatting. Return only the JSON response.  
      `
      }
    ],
    response_format: { type: "json_object" },
  };

  try {
    console.log("🟢 [DEBUG] Sending request to OpenAI:", JSON.stringify(requestPayload, null, 2));
    const response = await openai.chat.completions.create(requestPayload);
    const extractedDetails = response.choices[0]?.message?.content;

    if (!extractedDetails) {
      throw new Error("OpenAI returned an empty response");
    }

    const parsedDetails = JSON.parse(extractedDetails);

    // Schema validation (recommended)
    const eventSchema = z.object({
      title: z.string(),
      description: z.string().optional(),
      all_day_event: z.boolean(),
      start_date: z.string().regex(/^\d{4}-\d{2}-\d{2}$|^$/),
      start_time: z.string().regex(/^\d{2}:\d{2}$|^$/),
      end_date: z.string().regex(/^\d{4}-\d{2}-\d{2}$|^$/),
      end_time: z.string().regex(/^\d{2}:\d{2}$|^$/),
    });

    const validation = eventSchema.safeParse(parsedDetails);
    if (!validation.success) {
      console.error("Validation failed:", validation.error);
      throw new Error("Schema validation failed.");
    }

    return validation.data;

  } catch (error) {
    console.error("Error extracting event details:", error);
    throw error;
  }
}

/**
 * Extracts multiple event details from notification text using OpenAI API.
 * @param {string} notificationText - The notification text to process.
 * @returns {Promise<Object[]>} - Extracted events as an array of objects.
 */
async function email_extractEventDetails(notificationText) {
  const requestPayload = {
    model: "gpt-4o",
    messages: [
      {
        role: "user",
        content: `
        You are an intelligent assistant that extracts **multiple** calendar events from notifications.  
        You **must** respond strictly in valid JSON matching this format:
        \`\`\`json
        {
          "events": [
            {
              "title": "Event Title",
              "description": "Optional event description",
              "all_day_event": false,
              "start_date": "YYYY-MM-DD",
              "start_time": "HH:MM",
              "end_date": "YYYY-MM-DD",
              "end_time": "HH:MM"
            },
            {
              "title": "Another Event",
              "description": "Optional description",
              "all_day_event": true,
              "start_date": "YYYY-MM-DD",
              "start_time": "",
              "end_date": "",
              "end_time": ""
            }
          ]
        }
        \`\`\`

        **Notification Details:**  
        - Notification text: "${notificationText}"  

        ### **Rules for Extracting Events:**  
        1. **Extract Multiple Events**  
           - If the notification contains multiple event mentions, extract **each one separately**.  
           - If only one event is present, return an array inside the "events" object with a **single** object.  

        2. **Date & Time Interpretation**  
           - Interpret relative dates/times based on the received timestamp.  
           - "Next [weekday]" refers to the **weekday of the following week**.  
           - "This [weekday]" refers to the **upcoming occurrence of that weekday**.  

        3. **Handling Missing Information**  
           - **Do NOT assume or infer missing end date or time.**  
           - If the end date or end time is not explicitly mentioned, set "end_date": "" and "end_time": "".  
           - If the event time is not given, set "start_time": "" and "end_time": "".  
           - If any detail is missing or unclear, return it as an empty string "" instead of making assumptions.  

        4. **All-Day Events**  
           - If explicitly stated as "all day," set "all_day_event": true and leave "start_time" and "end_time" empty.  

        **DO NOT** include extra text or formatting. Return only the JSON response.  
        `
      }
    ],
    response_format: { type: "json_object" },
  };

  try {
    console.log("🟢 [DEBUG] Sending request to OpenAI:", JSON.stringify(requestPayload, null, 2));

    const response = await openai.chat.completions.create(requestPayload);
    const extractedDetails = response.choices[0]?.message?.content;

    if (!extractedDetails) {
        console.error("❌ [ERROR] OpenAI returned an empty response");
        throw new Error("OpenAI returned an empty response");
    }

    let parsedDetails = JSON.parse(extractedDetails);

    // Log extracted details before modifying
    console.log("🟢 [DEBUG] Raw extracted details:", JSON.stringify(parsedDetails, null, 2));

    if (!Array.isArray(parsedDetails.events)) {
        console.warn("⚠️ OpenAI returned a single event object instead of an array. Wrapping it in an array.");
        parsedDetails.events = [parsedDetails];
    }

    console.log("✅ [DEBUG] Parsed and corrected extracted events:", JSON.stringify(parsedDetails.events, null, 2));

    return parsedDetails.events;
} catch (error) {
    console.error("❌ [ERROR] Failed in email_extractEventDetails:", error);
    throw error;
}
}

module.exports = { extractEventDetails, email_extractEventDetails };
