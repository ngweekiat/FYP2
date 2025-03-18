import { createContext, useContext, useEffect, useState } from "react";
import { auth, provider } from "./firebase";
import { signInWithPopup, signOut, onAuthStateChanged, deleteUser } from "firebase/auth";
import { db } from "./firebase"; // Firebase Firestore instance
import { doc, setDoc, getDoc } from "firebase/firestore";
import { reauthenticateWithPopup } from "firebase/auth";


const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [users, setUsers] = useState([]);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (currentUser) => {
      if (currentUser) {
        const token = await currentUser.getIdToken(true); // 🔄 Refresh Token if needed
        setUsers([{ ...currentUser, accessToken: token }]);
      }
    });

    return () => unsubscribe();
  }, []);

  const handleSignIn = async () => {
    try {
      provider.addScope("https://www.googleapis.com/auth/calendar.events"); 
      provider.setCustomParameters({ prompt: "select_account" });

      const result = await signInWithPopup(auth, provider);
      const newUser = result.user;
      const accessToken = result._tokenResponse.oauthAccessToken; // ✅ OAuth Access Token for Google Calendar
      const refreshToken = result.user.stsTokenManager.refreshToken; // ✅ Firebase refresh token (limited)

      setUsers([{ ...newUser, accessToken }]);

      // ✅ Store user credentials in Firestore
      await saveUserCredentials(newUser.uid, newUser.email, newUser.displayName, accessToken, refreshToken);
    } catch (error) {
      console.error("Error signing in: ", error);
    }
  };

  const handleSignOut = async (uid) => {
    try {
        const user = auth.currentUser;
        if (!user || user.uid !== uid) return;

        // ✅ Ensure user is recently authenticated before deletion
        try {
            const result = await reauthenticateWithPopup(user, provider);
            console.log("User reauthenticated:", result);
        } catch (reauthError) {
            console.error("Reauthentication failed:", reauthError);
            alert("Please sign in again before deleting your account.");
            return;
        }

        // ✅ Now safe to delete user
        await deleteUser(user);
        console.log("User account deleted successfully.");

        await signOut(auth);
        setUsers([]);
    } catch (error) {
        console.error("Error signing out:", error);
    }
};

const saveUserCredentials = async (uid, email, displayName, accessToken, refreshToken) => {
  try {
      const response = await fetch("http://localhost:3000/api/users/save-user-web", { // ✅ Call backend route
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
              uid,
              email,
              displayName,
              accessToken,
              refreshToken
          }),
      });

      if (!response.ok) {
          throw new Error(`Failed to store user tokens: ${response.statusText}`);
      }

      const data = await response.json();
      console.log("User tokens stored successfully!", data);

  } catch (error) {
      console.error("Error storing tokens via API:", error);
  }
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
