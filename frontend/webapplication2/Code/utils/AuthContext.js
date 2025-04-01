import { createContext, useContext, useState, useEffect } from "react";
import { useGoogleLogin } from "@react-oauth/google";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [users, setUsers] = useState([]);

  // 🔁 Restore user from localStorage
  useEffect(() => {
    const storedUsers = localStorage.getItem("users");
    if (storedUsers) {
      setUsers(JSON.parse(storedUsers));
    }
  }, []);

  // 💾 Save user to localStorage whenever it changes
  useEffect(() => {
    localStorage.setItem("users", JSON.stringify(users));
  }, [users]);

  const handleSignIn = useGoogleLogin({
    flow: "implicit",
    scope:
      "https://www.googleapis.com/auth/calendar.events https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email",
    onSuccess: async (tokenResponse) => {
      try {
        const accessToken = tokenResponse.access_token;

        const res = await fetch("https://www.googleapis.com/oauth2/v3/userinfo", {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        });

        const userInfo = await res.json();

        const user = {
          uid: userInfo.sub,
          email: userInfo.email,
          displayName: userInfo.name,
          photoURL: userInfo.picture,
          accessToken,
        };

        setUsers([user]); // You can allow multiple accounts if needed
      } catch (err) {
        console.error("❌ Error fetching user info:", err);
      }
    },
    onError: (error) => {
      console.error("❌ Login error:", error);
    },
  });

  const handleSignOut = (uid) => {
    const updatedUsers = users.filter((u) => u.uid !== uid);
    setUsers(updatedUsers);
    localStorage.setItem("users", JSON.stringify(updatedUsers));
  };

  return (
    <AuthContext.Provider value={{ users, handleSignIn, handleSignOut }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
