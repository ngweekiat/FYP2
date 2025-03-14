import axios from "axios";

export default async function handler(req, res) {
  try {
    const response = await axios.get("http://localhost:3000/api/notifications/calendar_events/"); // Replace with your backend URL
    console.log("API Response:", response.data); // Log response data
    res.status(200).json(response.data);
  } catch (error) {
    console.error("Error fetching notifications:", error);
    res.status(500).json({ message: "Error fetching notifications", error: error.message });
  }
}
