// 📁 components/email_ExtractedEvent.js
import { motion } from "framer-motion";
import { useState, useEffect } from "react";
import { handleSaveEvent, handleDiscardEvent } from "../utils/eventHandlers";
import { useAuth } from "../utils/AuthContext";

export default function ExtractedEventsPanel({ emailData = [], onAddEvent = () => {}, onDiscard }) {
  const [events, setEvents] = useState([]);
  const [addedEvents, setAddedEvents] = useState(new Set());
  const [showPopup, setShowPopup] = useState(false);
  const { users } = useAuth();
  const accessToken = users[0]?.accessToken;

  useEffect(() => {
    if (emailData.length > 0) {
      const generateEventId = (event) => {
        const baseTitle = (event.title || "event")
          .toLowerCase()
          .replace(/[^a-z0-9-_.~]/g, "-")  // Replace invalid chars
          .slice(0, 40); // Truncate to ensure we don’t go over the max length
      
        const timestamp = Date.now().toString(36); // shorter unique encoding
        return `${baseTitle}-${timestamp}`;
      };
      

      setEvents(emailData.map(event => ({
        id: event.id || generateEventId(),
        title: event.subject || event.title || "Untitled Event",
        description: event.body || event.description || "",
        allDay: event.allDay || false,
        start_date: event.start_date || "",
        start_time: event.start_time || "",
        end_date: event.end_date || event.start_date || "",
        end_time: event.end_time || event.start_time || "",
        location: event.location || "",
        attendees: event.attendees || [],
      })));
    }
  }, [emailData]);

  const handleInputChange = (id, field, value) => {
    setEvents(prevEvents =>
      prevEvents.map(event => (event.id === id ? { ...event, [field]: value } : event))
    );
  };

  const handleSave = async (event) => {
    try {
      await handleSaveEvent(event, setEvents, setShowPopup, accessToken);
      setAddedEvents(prevAdded => new Set([...prevAdded, event.id]));
      if (typeof onAddEvent === "function") {
        onAddEvent(event);
      }
    } catch (error) {
      console.error("🚨 Error saving event:", error);
    }
  };

  const handleDiscard = async (id) => {
    try {
      await handleDiscardEvent(id, setEvents, setShowPopup, accessToken);
      setAddedEvents(prevAdded => {
        const newSet = new Set(prevAdded);
        newSet.delete(id);
        return newSet;
      });
    } catch (error) {
      console.error("🚨 Error discarding event:", error);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, x: "100%" }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ type: "spring", stiffness: 100 }}
      className="p-6 w-full"
    >
      <h2 className="text-2xl font-semibold text-gray-800 mb-4 -mt-2 text-left w-full border-b-2 border-gray-300 pb-2">
        Extracted Events
      </h2>
      {events.length > 0 ? (
        events.map((event) => (
          <div key={event.id} className="mb-6 p-4 border rounded-lg bg-gray-50 shadow">
            <input
              type="text"
              placeholder="Event Title"
              value={event.title}
              onChange={(e) => handleInputChange(event.id, "title", e.target.value)}
              className="w-full p-2 border border-gray-300 rounded-md mb-3"
            />
            <textarea
              placeholder="Description"
              value={event.description}
              onChange={(e) => handleInputChange(event.id, "description", e.target.value)}
              className="w-full p-2 border border-gray-300 rounded-md mb-3"
            />
            <div className="flex justify-between items-center mb-4">
              <span className="text-gray-700">All Day</span>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={event.allDay}
                  onChange={() => handleInputChange(event.id, "allDay", !event.allDay)}
                  className="sr-only peer"
                />
                <div className="w-10 h-5 bg-gray-300 rounded-full peer peer-checked:bg-blue-600 transition"></div>
              </label>
            </div>
            <div className="flex gap-3 mb-3">
              <input
                type="date"
                value={event.start_date || ""}
                onChange={(e) => handleInputChange(event.id, "start_date", e.target.value)}
                className="flex-1 p-2 border border-gray-300 rounded-md"
              />
              <input
                type="time"
                value={event.start_time || ""}
                onChange={(e) => handleInputChange(event.id, "start_time", e.target.value)}
                className="flex-1 p-2 border border-gray-300 rounded-md"
                disabled={event.allDay}
              />
            </div>
            <div className="flex gap-3 mb-3">
              <input
                type="date"
                value={event.end_date || ""}
                onChange={(e) => handleInputChange(event.id, "end_date", e.target.value)}
                className="flex-1 p-2 border border-gray-300 rounded-md"
              />
              <input
                type="time"
                value={event.end_time || ""}
                onChange={(e) => handleInputChange(event.id, "end_time", e.target.value)}
                className="flex-1 p-2 border border-gray-300 rounded-md"
                disabled={event.allDay}
              />
            </div>
            <input
              type="text"
              placeholder="Location"
              value={event.location}
              onChange={(e) => handleInputChange(event.id, "location", e.target.value)}
              className="w-full p-2 border border-gray-300 rounded-md mb-4"
            />
            <div className="flex justify-between">
              {addedEvents.has(event.id) ? (
                <span className="text-blue-600 font-semibold">Added!</span>
              ) : (
                <button
                  onClick={() => handleSave(event)}
                  className="bg-blue-600 text-white px-4 py-2 rounded-lg shadow-md hover:bg-blue-700 transition"
                >
                  Add Event
                </button>
              )}
              <button
                onClick={() => handleDiscard(event.id)}
                className="text-red-600 hover:text-red-800"
              >
                Discard Event
              </button>
            </div>
          </div>
        ))
      ) : (
        <p className="text-center text-gray-500">No events detected</p>
      )}
    </motion.div>
  );
}