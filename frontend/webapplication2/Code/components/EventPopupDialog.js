import { useState } from "react";

export default function EventPopupDialog({ eventDetails, onAdd, onDiscard, onClose }) {
  const [title, setTitle] = useState(eventDetails?.title || "");
  const [description, setDescription] = useState(eventDetails?.description || "");
  const [allDay, setAllDay] = useState(eventDetails?.all_day_event || false);
  const [startDate, setStartDate] = useState(eventDetails?.start_date || "");
  const [startTime, setStartTime] = useState(eventDetails?.start_time || "");
  const [endDate, setEndDate] = useState(eventDetails?.end_date || "");
  const [endTime, setEndTime] = useState(eventDetails?.end_time || "");
  const [location, setLocation] = useState(eventDetails?.location || "");  

  const handleSave = () => {
    onAdd({
      ...eventDetails,
      title,
      description,
      all_day_event: allDay, // Match backend property
      start_date: startDate, // Correct key names
      start_time: allDay ? "" : startTime, // Clear time if "All Day" is enabled
      end_date: endDate,
      end_time: allDay ? "" : endTime,
      location,
    });
    onClose();
  };
  

  return (
    <div 
      className="fixed inset-0 flex items-center justify-center"
      style={{ backgroundColor: "rgba(26, 26, 26, 0.72)" }}
      onClick={onClose}
    >
      <div 
        className="bg-white p-6 rounded-lg shadow-lg w-[90%] max-w-md relative"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex justify-between items-center mb-4">
          <button onClick={onClose} className="text-gray-600 hover:text-gray-900">✖</button>
          <h2 className="text-lg font-bold">Add Event</h2>
          <button onClick={handleSave} className="text-blue-600 hover:text-blue-800">Add</button>
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

        {/* "All Day" Toggle */}
        <div className="flex justify-between items-center mb-4">
          <span className="text-gray-700">All Day</span>
          <label className="relative inline-flex items-center cursor-pointer">
            <input 
              type="checkbox" 
              checked={allDay} 
              onChange={() => setAllDay(!allDay)} 
              className="sr-only peer"
            />
            <div className="w-10 h-5 bg-gray-300 rounded-full peer peer-checked:bg-blue-600 transition"></div>
          </label>
        </div>

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
            disabled={allDay} // Disable when "All Day" is checked
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
            disabled={allDay} // Disable when "All Day" is checked
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
