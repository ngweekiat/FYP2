import { GoogleGenerativeAI } from "@google/generative-ai";

const genAI = new GoogleGenerativeAI("AIzaSyDcpjLErwLkqoqCnvcKl6557PKKG86nkPM");

export const sendToGemini = async (notificationText) => {
  if (!notificationText.trim()) return [];

  try {
    const model = genAI.getGenerativeModel({
      model: "gemini-1.5-flash",
      generationConfig: {
        temperature: 0.2,
        topK: 20,
        topP: 0.9,
        maxOutputTokens: 1024,
        responseMimeType: "text/plain",
      },
    });

    const receivedAtTimestamp = new Date().toISOString();

    const prompt = `
You are a smart assistant that extracts **all calendar events** mentioned in a block of text or chat history.

Return the result as a **JSON array** of objects. Each object must have this format:
{
  "title": "Event Title",
  "description": "Optional event description",
  "location": "Event location or meeting link",
  "all_day_event": false,
  "start_date": "YYYY-MM-DD",
  "start_time": "HH:MM",
  "end_date": "YYYY-MM-DD",
  "end_time": "HH:MM"
}

Rules:
1. Extract all event-like structures, not just the last one.
2. Interpret relative dates like "tomorrow" based on: "${receivedAtTimestamp}".
3. If end time is missing, assume 1-hour duration from start time.
4. If only a date is present, leave time fields empty and set "all_day_event": true.
5. If any field is missing, leave it as "".
6. Return only the **JSON array**. Do not include explanations or comments.

Text:
${notificationText}
`.trim();

    const result = await model.generateContent(prompt);
    const raw = await result.response.text();
    console.log("📤 Gemini raw response:", raw);

    // Extract the JSON array from the raw response
    const jsonStart = raw.indexOf("[");
    const jsonEnd = raw.lastIndexOf("]");

    if (jsonStart === -1 || jsonEnd === -1) {
      console.error("❌ No valid JSON array found in Gemini response.");
      return [];
    }

    const jsonString = raw.substring(jsonStart, jsonEnd + 1);
    const parsedArray = JSON.parse(jsonString);

    // Safely map each event
    const formattedEvents = parsedArray.map(event => ({
      id: Math.random().toString(36).substr(2, 9),
      title: event.title || "",
      description: event.description || "",
      allDay: event.all_day_event || false,
      start_date: event.start_date || "",
      start_time: event.start_time || "",
      end_date: event.end_date || "",
      end_time: event.end_time || "",
      location: event.location || "",
      attendees: event.attendees || [],
    }));

    return formattedEvents;
  } catch (err) {
    console.error("❌ [Gemini Error]:", err);
    return [];
  }
};
