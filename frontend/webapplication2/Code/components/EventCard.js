export default function EventCard({ event }) {
    return (
      <div className="bg-white shadow-md rounded-lg p-4 border border-gray-200">
        <h2 className="text-lg font-semibold">{event.title}</h2>
        <p className="text-sm text-gray-500">{event.description}</p>
        <p className="text-blue-600 text-sm font-semibold">{event.date}</p>
      </div>
    );
  }
  