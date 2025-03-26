package com.example.fyp_androidapp.data.repository

import android.content.Context
import android.util.Log
import com.example.fyp_androidapp.Constants
import com.example.fyp_androidapp.database.dao.UserDao
import com.example.fyp_androidapp.database.entities.UserEntity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject

class AuthRepository(
    private val userDao: UserDao
) {
    private val client = OkHttpClient()

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("410405106281-k82mf5kndd5e3vs1u01gg9hiihq8pe47.apps.googleusercontent.com")
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/calendar.events"))
            .requestServerAuthCode("410405106281-k82mf5kndd5e3vs1u01gg9hiihq8pe47.apps.googleusercontent.com", true)
            .build()
        return com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
    }

    suspend fun signInWithGoogle(
        uid: String,
        email: String?,
        displayName: String?,
        idToken: String?,
        authCode: String?
    ) {
        withContext(Dispatchers.IO) {
            val (accessToken, refreshToken) = requestTokensFromAuthCode(authCode)

            val user = UserEntity(
                uid = uid,
                email = email ?: "Unknown",
                displayName = displayName ?: "Unknown",
                idToken = idToken,
                authCode = authCode,
                accessToken = accessToken,
                refreshToken = refreshToken
            )
            userDao.insertUser(user)
        }
    }

    suspend fun signOut(uid: String) = withContext(Dispatchers.IO) {
        userDao.deleteUserById(uid)
    }

    private fun requestTokensFromAuthCode(authCode: String?): Pair<String?, String?> {
        if (authCode == null) return Pair(null, null)
        return try {
            val url = "https://oauth2.googleapis.com/token"
            val requestBody = FormBody.Builder()
                .add("code", authCode)
                .add("client_id", "410405106281-k82mf5kndd5e3vs1u01gg9hiihq8pe47.apps.googleusercontent.com")
                .add("client_secret", "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")
                .add("redirect_uri", "") // optional
                .add("grant_type", "authorization_code")
                .build()

            val request = Request.Builder().url(url).post(requestBody).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("TokenExchange", "Failed: ${response.code}")
                    return Pair(null, null)
                }

                val json = JSONObject(response.body?.string() ?: "")
                Pair(
                    json.optString("access_token", null),
                    json.optString("refresh_token", null)
                )
            }
        } catch (e: Exception) {
            Log.e("TokenExchange", "Error: $e")
            Pair(null, null)
        }
    }
}
