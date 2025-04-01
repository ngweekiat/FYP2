import { initializeApp } from "firebase/app";
import { getAuth, GoogleAuthProvider } from "firebase/auth";

// Replace these with your actual Firebase configuration values
const firebaseConfig = {
    apiKey: "AIzaSyDEhrj7AQCKCu03rPQsD8JwEdt0NMwnnH8",
    authDomain: "fypeventcalendar2.firebaseapp.com",
    databaseURL: "https://fypeventcalendar2-default-rtdb.firebaseio.com",
    projectId: "fypeventcalendar2",
    storageBucket: "fypeventcalendar2.firebasestorage.app",
    messagingSenderId: "410405106281",
    appId: "1:410405106281:web:c3c5c02389eb8541df68f8",
    measurementId: "G-TR730ECQNL"
  };

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const provider = new GoogleAuthProvider();

export { auth, provider };
