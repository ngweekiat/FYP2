import { useEffect, useState } from "react";
import { auth, provider } from "../utils/firebase";
import { signInWithPopup, signOut, onAuthStateChanged } from "firebase/auth";
import { LogIn, LogOut, Link2 } from "lucide-react";

export default function SettingsPage() {
  const [users, setUsers] = useState([]); // Store multiple authenticated users

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
      if (currentUser) {
        setUsers((prevUsers) => {
          const exists = prevUsers.some((user) => user.uid === currentUser.uid);
          return exists ? prevUsers : [...prevUsers, currentUser];
        });
      }
    });

    return () => unsubscribe();
  }, []);

  const handleSignIn = async () => {
    try {
      const result = await signInWithPopup(auth, provider);
      const newUser = result.user;
      
      setUsers((prevUsers) => {
        const exists = prevUsers.some((user) => user.uid === newUser.uid);
        return exists ? prevUsers : [...prevUsers, newUser];
      });
    } catch (error) {
      console.error("Error signing in: ", error);
    }
  };

  const handleSignOut = async (uid) => {
    try {
      await signOut(auth);
      setUsers((prevUsers) => prevUsers.filter((user) => user.uid !== uid));
    } catch (error) {
      console.error("Error signing out: ", error);
    }
  };

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
              <button
                onClick={() => handleSignOut(user.uid)}
                className="bg-gradient-to-r from-red-500 to-red-700 text-white px-3 py-2 rounded-lg shadow-lg hover:from-red-600 hover:to-red-800 transition-all"
              >
                <LogOut size={16} />
              </button>
            </div>
          ))}
        </div>
      )}

      {users.length > 0 && (
        <div className="mt-6 bg-gray-50 p-4 rounded-xl shadow-md">
          <h2 className="text-xl font-semibold text-gray-800">Google Calendar Sync</h2>
          <p className="text-sm text-gray-500">Link your Google Calendar to manage events.</p>
          <button className="mt-4 w-full flex items-center justify-center gap-2 bg-gradient-to-r from-green-500 to-green-700 text-white px-6 py-3 rounded-xl shadow-lg hover:from-green-600 hover:to-green-800 transition-all">
            <Link2 size={20} />
            Link Google Calendar
          </button>
        </div>
      )}
    </div>
  );
}
