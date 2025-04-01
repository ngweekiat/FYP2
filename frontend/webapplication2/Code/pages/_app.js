import Layout from "../components/Layout";
import "../styles/globals.css";
import { AuthProvider } from "../utils/AuthContext";
import { GoogleOAuthProvider } from "@react-oauth/google";

function MyApp({ Component, pageProps }) {
  return (
    <GoogleOAuthProvider clientId="708853968110-968spd4japerl0vkdia82dl63l04bvuu.apps.googleusercontent.com">
      <AuthProvider>
        <Layout>
          <Component {...pageProps} />
        </Layout>
      </AuthProvider>
    </GoogleOAuthProvider>
  );
}

export default MyApp;
