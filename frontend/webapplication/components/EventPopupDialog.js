import { useState } from "react";
import { formatDateOnly, formatTimeOnly } from "../utils/dateUtils"; // ✅ Reuse date formatters

export default function EventPopupDialog({ eventDetails, onSave, onDiscard, onClose }) {
  const [title, setTitle] = useState(eventDetails?.title || "");
  const [description, setDescription] = useState(eventDetails?.description || "");
  const [startDate, setStartDate] = useState(eventDetails?.startDate || "");
  const [startTime, setStartTime] = useState(eventDetails?.startTime || "");
  const [endDate, setEndDate] = useState(eventDetails?.endDate || "");
  const [endTime, setEndTime] = useState(eventDetails?.endTime || "");
  const [location, setLocation] = useState(eventDetails?.locationOrMeeting || "");

  const handleSave = () => {
    onSave({
      ...eventDetails,
      title,
      description,
      startDate,
      startTime,
      endDate,
      endTime,
      location,
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-gray-900 bg-opacity-50">
      <div className="bg-white p-6 rounded-lg shadow-md w-[90%] max-w-md">
        {/* Header */}
        <div className="flex justify-between items-center mb-4">
          <button onClick={onClose} className="text-gray-600 hover:text-gray-900">✖</button>
          <h2 className="text-lg font-bold">Add Event</h2>
          <button onClick={handleSave} className="text-blue-600 hover:text-blue-800">Save</button>
        </div>

        {/* Title Input */}
        <input
          type="text"
          placeholder="Add Title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="w-full p-2 border border-gray-300 rounded-md mb-3"
        />

        {/* Description Input */}
        <textarea
          placeholder="Add Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          className="w-full p-2 border border-gray-300 rounded-md mb-3"
        />

        {/* Start Date & Time */}
        <div className="flex gap-3 mb-3">
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            className="flex-1 p-2 border border-gray-300 rounded-md"
          />
          <input
            type="time"
            value={startTime}
            onChange={(e) => setStartTime(e.target.value)}
            className="flex-1 p-2 border border-gray-300 rounded-md"
          />
        </div>

        {/* End Date & Time */}
        <div className="flex gap-3 mb-3">
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className="flex-1 p-2 border border-gray-300 rounded-md"
          />
          <input
            type="time"
            value={endTime}
            onChange={(e) => setEndTime(e.target.value)}
            className="flex-1 p-2 border border-gray-300 rounded-md"
          />
        </div>

        {/* Location Input */}
        <input
          type="text"
          placeholder="Location"
          value={location}
          onChange={(e) => setLocation(e.target.value)}
          className="w-full p-2 border border-gray-300 rounded-md mb-4"
        />

        {/* Discard Button */}
        <button
          onClick={() => {
            onDiscard(eventDetails.id);
            onClose();
          }}
          className="w-full text-red-600 hover:text-red-800"
        >
          Discard Event
        </button>
      </div>
    </div>
  );
}
