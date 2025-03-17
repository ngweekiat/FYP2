package com.example.fyp_androidapp.data.repository

import android.util.Log
import com.example.fyp_androidapp.Constants
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val client = OkHttpClient()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun signInWithGoogle(idToken: String?): FirebaseUser? {
        return withContext(Dispatchers.IO) {
            if (idToken == null) {
                Log.e("AuthRepository", "ID Token is null. Aborting sign-in.")
                return@withContext null
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            try {
                val authResult = auth.signInWithCredential(credential).await()
                authResult.user
            } catch (e: Exception) {
                Log.e("AuthRepository", "Google sign-in failed", e)
                null
            }
        }
    }

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            auth.signOut()
        }
    }

    fun sendTokenToBackend(userId: String, email: String?, displayName: String?, idToken: String?, authCode: String?) {
        if (idToken == null) {
            Log.e("sendTokenToBackend", "ID Token is null. Aborting request.")
            return
        }

        val requestBody = JSONObject().apply {
            put("uid", userId)
            put("email", email ?: "Unknown")
            put("displayName", displayName ?: "Unknown")
            put("idToken", idToken)
            put("authCode", authCode)
        }.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${Constants.BASE_URL}/users/save-user")
            .post(requestBody)
            .build()

        // Use withContext to move network call to background thread
        try {
            // Make sure the network operation runs in the background
            GlobalScope.launch(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("sendTokenToBackend", "Failed to send token: ${response.code} - ${response.message}")
                    } else {
                        Log.d("sendTokenToBackend", "Token sent successfully: ${response.code}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("sendTokenToBackend", "Error sending token to backend", e)
        }
    }
}
