import { useEffect, useState } from "react";
import axios from "axios";
import NotificationCard from "../components/notificationCard";
import EventPopupDialog from "../components/EventPopupDialog";
import { handleSaveEvent, handleDiscardEvent } from "../utils/eventHandlers";


export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [events, setEvents] = useState({});
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [lastVisible, setLastVisible] = useState(null);
  const [showPopup, setShowPopup] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);

  const BACKEND_URL = "http://localhost:3000/api/notifications";

  // Fetch notifications
  const fetchNotifications = async (limit = 20) => {
    if (loading) return;
    setLoading(true);

    try {
      const url = `${BACKEND_URL}?limit=${limit}${lastVisible ? `&startAfter=${lastVisible}` : ""}`;
      const response = await axios.get(url);
      const newNotifications = response.data.notifications || [];

      setNotifications((prev) => [...prev, ...newNotifications]);

      if (response.data.lastVisible) {
        setLastVisible(response.data.lastVisible);
      }

      const importantNotificationIds = newNotifications
        .filter((n) => n.notification_importance === 1)
        .map((n) => n.id);

      if (importantNotificationIds.length > 0) {
        await fetchEvents(importantNotificationIds);
      }
    } catch (error) {
      console.error("Error fetching notifications:", error);
    } finally {
      setLoading(false);
      setInitialLoading(false);
    }
  };

  // Fetch calendar events for the notifications
  const fetchEvents = async (notificationIds) => {
    try {
      const eventRequests = notificationIds.map((id) =>
        axios.get(`${BACKEND_URL}/calendar_events/${id}`).catch(() => null)
      );

      const eventResponses = await Promise.all(eventRequests);
      const eventData = {};

      eventResponses.forEach((response, index) => {
        if (response) {
          if (response.data.event) {
            eventData[notificationIds[index]] = response.data.event;
          }
        }
      });

      console.log("📅 Events fetched for notifications:", eventData);


      setEvents(eventData);
    } catch (error) {
      console.error("🚨 Error fetching events:", error);
    }
  };

  // ✅ Function to handle adding an event (Opens Popup)
  const onAddEvent = (event) => {
    setSelectedEvent(event);
    setShowPopup(true);
  };

  const handleSaveEventWrapper = (updatedEvent) => {
    handleSaveEvent(updatedEvent, setEvents, setShowPopup);
  };
  
  const handleDiscardEventWrapper = (eventId) => {
    handleDiscardEvent(eventId, setEvents, setShowPopup);
  };
  

  useEffect(() => {
    fetchNotifications();
    
  }, []);

  // Infinite scrolling (load more on scroll)
  useEffect(() => {
    const handleScroll = () => {
      const scrollableDiv = document.documentElement || document.body;
      if (scrollableDiv.scrollHeight - window.innerHeight - scrollableDiv.scrollTop < 100 && !loading) {
        fetchNotifications();
      }
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [loading]);

  return (
    <div className="p-6 h-screen flex flex-col">
      <h1 className="text-2xl font-bold">Notifications</h1>

      {/* Scrollable Container */}
      <div className="flex-1 overflow-y-auto space-y-4 relative">
        {initialLoading ? (
          <div className="flex justify-center items-center h-full">
            <div className="flex flex-col items-center">
              <div className="animate-spin h-8 w-8 border-4 border-blue-500 border-t-transparent rounded-full"></div>
              <p className="mt-2 text-lg font-semibold text-gray-700">Fetching Notifications...</p>
            </div>
          </div>
        ) : (
          notifications.map((notification) => (
            <NotificationCard
              key={notification.id}
              notification={notification}
              event={events[notification.id] || null}
              onAddEvent={onAddEvent} // ✅ Pass function to open popup
              onDiscardEvent={handleDiscardEventWrapper} // ✅ Pass function to discard event
            />
          ))
        )}

        {/* Small Loading Indicator for Infinite Scrolling */}
        {loading && !initialLoading && (
          <p className="text-center text-gray-600">Loading more...</p>
        )}
      </div>

      {/* ✅ Show Popup When an Event is Selected */}
      {showPopup && selectedEvent && (
        <EventPopupDialog
          eventDetails={selectedEvent}
          onAdd={handleSaveEventWrapper}
          onDiscard={handleDiscardEventWrapper}
          onClose={() => setShowPopup(false)}
        />
      )}
    </div>
  );
}
