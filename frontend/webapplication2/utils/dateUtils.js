// Function to format the date as "MAR 18, 2025"
export function formatDateOnly(dateString) {
  if (!dateString) return "No Date";

  const date = new Date(dateString);
  if (isNaN(date)) return "Invalid Date";

  const months = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"];

  const day = String(date.getDate()).padStart(2, "0"); // Ensure 2-digit day
  const month = months[date.getMonth()]; // Get month abbreviation
  const year = date.getFullYear();

  return `${month} ${day}, ${year}`;
}

// Function to format the time as "01:06 AM"
export function formatTimeOnly(timeString) {
  if (!timeString) return "No Time";

  const [hours, minutes] = timeString.split(":").map(Number);
  if (isNaN(hours) || isNaN(minutes)) return "Invalid Time";

  const ampm = hours >= 12 ? "PM" : "AM";
  const formattedHours = hours % 12 || 12; // Convert 24-hour format to 12-hour

  return `${formattedHours}:${String(minutes).padStart(2, "0")}${ampm}`;
}


// Function to format the time from an ISO 8601 timestamp (e.g., "2025-03-18T06:16:28Z") to "06:16 AM"
export function formatISOTimestamp(timestamp) {
  if (!timestamp) return "No Time";

  const date = new Date(timestamp);
  if (isNaN(date)) return "Invalid Time";

  const hours = date.getHours();
  const minutes = date.getMinutes();
  const ampm = hours >= 12 ? "PM" : "AM";
  const formattedHours = hours % 12 || 12; // Convert 24-hour format to 12-hour

  return `${formattedHours}:${String(minutes).padStart(2, "0")} ${ampm}`;
}

// Function to convert a date string or Date object to ISO 8601 format (e.g., "2025-03-18T06:16:28Z")
export function convertToISO(dateInput, timeInput = "00:00") {
  if (!dateInput) return "Invalid Date"; // Check for invalid input

  // Handle the case where dateInput is in the format "YYYY-MM-DD"
  const [year, month, day] = dateInput.split("-").map(Number);
  if (isNaN(year) || isNaN(month) || isNaN(day)) return "Invalid Date";

  // If a time is provided, combine it with the date (e.g., "2025-03-18T07:17")
  const time = timeInput || "00:00"; // Default to 00:00 if no time provided
  const dateTime = new Date(year, month - 1, day, ...time.split(":").map(Number)); // Combine date and time

  if (isNaN(dateTime)) return "Invalid Date"; // Ensure it's a valid Date

  return dateTime.toISOString(); // Convert to ISO 8601 format
}
