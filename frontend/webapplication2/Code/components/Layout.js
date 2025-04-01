// 📁 components/Layout.js
import { useRouter } from "next/router";
import Link from "next/link";
import { Mail, Settings, Plus, User } from "lucide-react";
import { useAuth } from "../utils/AuthContext";

export default function Layout({ children }) {
  const router = useRouter();
  const { users, handleSignIn } = useAuth();

  return (
    <div className="flex h-screen bg-gray-100 overflow-hidden">
      {/* Sidebar */}
      <div className="bg-blue-500 text-white w-16 flex flex-col items-center py-4 space-y-6 relative h-full">
        {/* Navigation Links */}
        {[
          { name: "Email", path: "/email_page", icon: Mail },
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

        {/* Floating Plus Button - Sign In Only */}
        <button
          onClick={users.length === 0 ? handleSignIn : null}
          disabled={users.length > 0}
          className={`bg-white text-blue-900 p-3 rounded-full shadow-lg transition ${
            users.length > 0 ? "opacity-50 cursor-not-allowed" : "hover:bg-gray-200"
          }`}
          title={users.length > 0 ? "Already signed in" : "Sign in with Google"}
        >
          <Plus size={24} />
        </button>

        {/* Stacked Profile Icons at the Bottom */}
        <div className="flex flex-col items-center gap-y-3 mt-4">
          {users.length > 0 ? (
            users.map((user) => (
              <img
                key={user.uid}
                src={user.photoURL || "/default-avatar.png"}
                alt="User"
                className="w-10 h-10 rounded-full border-2 border-yellow-500 shadow-md"
              />
            ))
          ) : (
            <button
              onClick={handleSignIn}
              className="bg-gradient-to-tr from-blue-700 to-pink-500 p-1 rounded-full border border-yellow-500"
            >
              <User size={24} />
            </button>
          )}
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top Navbar */}
        <div className="bg-white shadow-md p-4 flex justify-between">
          <span className="font-bold text-2xl">Event Extraction Dashboard</span>
        </div>

        {/* Page Content */}
        <div className="p-6 overflow-y-auto">{children}</div>
      </div>
    </div>
  );
}
