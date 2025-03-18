import { createContext, useContext, useEffect, useState } from "react";
import { auth, provider } from "./firebase";
import { signInWithPopup, signOut, onAuthStateChanged, deleteUser } from "firebase/auth";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [users, setUsers] = useState([]);

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
      const user = auth.currentUser;
      if (user && user.uid === uid) {
        await deleteUser(user); // Delete the user's authentication account
        console.log("User account deleted successfully.");
      }

      await signOut(auth);
      setUsers((prevUsers) => prevUsers.filter((user) => user.uid !== uid));
    } catch (error) {
      console.error("Error signing out or deleting account: ", error);
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
