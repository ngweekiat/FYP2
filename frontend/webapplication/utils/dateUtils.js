export function formatDate(timestamp) {
    if (!timestamp) return "No Timestamp";
  
    const date = new Date(timestamp);
  
    // Define month abbreviations in uppercase
    const months = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"];
  
    const day = String(date.getDate()).padStart(2, "0"); // Ensure 2-digit day
    const month = months[date.getMonth()]; // Get month abbreviation
    const year = date.getFullYear();
  
    let hours = date.getHours();
    const minutes = String(date.getMinutes()).padStart(2, "0"); // Ensure 2-digit minutes
    const ampm = hours >= 12 ? "PM" : "AM";
    hours = hours % 12 || 12; // Convert 24-hour time to 12-hour format
  
    return `${month} ${day}, ${year} ${hours}:${minutes}${ampm}`;
  }
  