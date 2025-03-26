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
            .requestIdToken(Constants.GOOGLE_CLIENT_ID)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/calendar.events"))
            .requestServerAuthCode(Constants.GOOGLE_CLIENT_ID, true)
            .build()
        return com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
    }

    suspend fun getAllUsers(): List<UserEntity> = withContext(Dispatchers.IO) {
        userDao.getAllUsers()
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
                .add("client_id", Constants.GOOGLE_CLIENT_ID)
                .add("client_secret", Constants.GOOGLE_CLIENT_SECRET)
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

    suspend fun refreshAccessToken(refreshToken: String?): String? = withContext(Dispatchers.IO) {
        if (refreshToken == null) return@withContext null

        try {
            val url = "https://oauth2.googleapis.com/token"
            val requestBody = FormBody.Builder()
                .add("client_id", Constants.GOOGLE_CLIENT_ID)
                .add("client_secret", Constants.GOOGLE_CLIENT_SECRET)
                .add("refresh_token", refreshToken)
                .add("grant_type", "refresh_token")
                .build()

            val request = Request.Builder().url(url).post(requestBody).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("TokenRefresh", "Failed to refresh token: ${response.code}")
                    return@withContext null
                }

                val json = JSONObject(response.body?.string() ?: "")
                return@withContext json.optString("access_token", null)
            }
        } catch (e: Exception) {
            Log.e("TokenRefresh", "Error refreshing token", e)
            null
        }
    }


}
