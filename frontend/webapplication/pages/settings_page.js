import { useAuth } from "../utils/AuthContext";
import { LogIn, LogOut, Link2 } from "lucide-react";

export default function SettingsPage() {
  const { users, handleSignIn, handleSignOut } = useAuth();

  return (
    <div className="max-w-lg mx-auto mt-12 p-6 bg-white shadow-lg rounded-2xl">
      <h1 className="text-3xl font-extrabold text-gray-800 mb-2">Settings</h1>
      <p className="text-gray-500">Manage your Google Calendar and event preferences.</p>

      <button
        onClick={handleSignIn}
        className="mt-6 w-full flex items-center justify-center gap-2 bg-gradient-to-r from-blue-500 to-blue-700 text-white px-6 py-3 rounded-xl shadow-lg hover:from-blue-600 hover:to-blue-800 transition-all"
      >
        <LogIn size={20} />
        Sign in with Google
      </button>

      {users.length > 0 && (
        <div className="mt-6 bg-gray-50 p-4 rounded-xl shadow-md">
          <h2 className="text-xl font-semibold text-gray-800">Signed-In Accounts</h2>
          {users.map((user) => (
            <div key={user.uid} className="mt-4 flex items-center justify-between p-3 border rounded-lg bg-white shadow-sm">
              <div className="flex items-center space-x-4">
                <img 
                  src={user.photoURL || "/default-avatar.png"} 
                  alt="User" 
                  className="w-12 h-12 rounded-full border-2 border-blue-500 shadow-sm"
                />
                <div>
                  <p className="text-lg font-semibold text-gray-800">{user.displayName}</p>
                  <p className="text-sm text-gray-500">{user.email}</p>
                </div>
              </div>
              <button onClick={() => handleSignOut(user.uid)} className="bg-red-500 text-white px-3 py-2 rounded-lg shadow-lg">
                <LogOut size={16} />
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
