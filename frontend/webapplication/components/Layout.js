import { useRouter } from "next/router";
import Link from "next/link";
import { Home, Bell, Calendar, Settings, Plus, User } from "lucide-react"; // Icons

export default function Layout({ children }) {
  const router = useRouter();

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar */}
      <div className="bg-blue-500 text-white w-16 flex flex-col items-center py-4 space-y-6 relative">
        {/* User Avatar/Icon */}
        <div className="bg-blue-700 p-2 rounded-full">
          <span className="font-bold text-xl">A</span> {/* Placeholder for profile */}
        </div>

        {/* Navigation Links */}
        {[
          { name: "Home", path: "/", icon: Home },
          { name: "Notifications", path: "/notifications_page", icon: Bell },
          { name: "Calendar", path: "/calendar_page", icon: Calendar },
          { name: "Settings", path: "/settings_page", icon: Settings },
        ].map((item) => {
          const isActive = router.pathname === item.path;

          return (
            <Link key={item.path} href={item.path} className="flex flex-col items-center space-y-1">
              <div
                className={`flex justify-center items-center w-12 h-12 rounded-lg transition ${
                  isActive ? "border-2 border-white bg-blue-600" : "hover:bg-blue-600"
                }`}
              >
                <item.icon size={24} className="text-white" />
              </div>
              <span className="text-xs">{item.name}</span>
            </Link>
          );
        })}

        {/* Spacer to push buttons down */}
        <div className="flex-1"></div>

        {/* Floating Plus Button */}
        <button className="bg-white text-blue-900 p-3 rounded-full shadow-lg hover:bg-gray-200 transition">
          <Plus size={24} />
        </button>

        {/* Profile Icon at the Bottom */}
        <div className="bg-gradient-to-tr from-blue-700 to-pink-500 p-1 rounded-full border border-yellow-500">
          <User size={24} />
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        {/* Top Navbar */}
        <div className="bg-white shadow-md p-4 flex justify-between">
          <span className="font-bold">Event Extraction Dashboard</span>
          <button className="bg-blue-500 text-white px-4 py-2 rounded">Sync</button>
        </div>

        {/* Page Content */}
        <div className="p-6">{children}</div>
      </div>
    </div>
  );
}
