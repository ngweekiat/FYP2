import { useEffect, useState } from "react";
import axios from "axios";
import NotificationCard from "../components/NotificationCard";

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [lastVisible, setLastVisible] = useState(null);
  const [selectedNotification, setSelectedNotification] = useState(null);
  const [filter, setFilter] = useState("all"); // Added state for filter

  // Fetch notifications
  const fetchNotifications = async (limit = 20) => {
    if (loading) return;
    setLoading(true);

    try {
      const url = `/api/notifications_api?limit=${limit}${lastVisible ? `&startAfter=${lastVisible}` : ""}`;
      const response = await axios.get(url);

      const newNotifications = response.data.notifications || [];
      setNotifications((prev) => [...prev, ...newNotifications]);

      if (response.data.lastVisible) {
        setLastVisible(response.data.lastVisible);
      }
    } catch (error) {
      console.error("Error fetching notifications:", error);
    } finally {
      setLoading(false);
      setInitialLoading(false); // Hide "Fetching Notifications..." after initial fetch
    }
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

  // Filter notifications based on the selected filter
  const filteredNotifications = notifications.filter((notification) => {
    if (filter === "all") return true;
    if (filter === "added" && notification.status_message !== "Event Discarded") return true;
    if (filter === "discarded" && notification.status_message === "Event Discarded") return true;
    return false;
  });

  return (
    <div className="p-6 h-screen flex flex-col">
      <h1 className="text-2xl font-bold">Notifications</h1>

      {/* Filter buttons */}
      <div className="flex space-x-4 mb-4">
        <button
          className={`px-4 py-2 rounded-lg ${filter === "all" ? "bg-blue-500 text-white" : "bg-gray-200"}`}
          onClick={() => setFilter("all")}
        >
          All
        </button>
        <button
          className={`px-4 py-2 rounded-lg ${filter === "added" ? "bg-blue-500 text-white" : "bg-gray-200"}`}
          onClick={() => setFilter("added")}
        >
          Added
        </button>
        <button
          className={`px-4 py-2 rounded-lg ${filter === "discarded" ? "bg-blue-500 text-white" : "bg-gray-200"}`}
          onClick={() => setFilter("discarded")}
        >
          Discarded
        </button>
      </div>

      {/* Scrollable Container */}
      <div className="flex-1 overflow-y-auto space-y-4 relative">
        {/* Show "Fetching Notifications..." only on initial load */}
        {initialLoading ? (
          <div className="flex justify-center items-center h-full">
            <div className="flex flex-col items-center">
              <div className="animate-spin h-8 w-8 border-4 border-blue-500 border-t-transparent rounded-full"></div>
              <p className="mt-2 text-lg font-semibold text-gray-700">Fetching Notifications...</p>
            </div>
          </div>
        ) : (
          filteredNotifications.map((notification) => (
            <NotificationCard
              key={notification.id}
              notification={notification}
              onAdd={() => setSelectedNotification(notification)}
              onDiscard={() => {
                setNotifications(notifications.map((n) => (n.id === notification.id ? { ...n, status_message: "Event Discarded" } : n)));
              }}
            />
          ))
        )}

        {/* Small Loading Indicator for Infinite Scrolling */}
        {loading && !initialLoading && (
          <p className="text-center text-gray-600">Loading more...</p>
        )}
      </div>
    </div>
  );
}
