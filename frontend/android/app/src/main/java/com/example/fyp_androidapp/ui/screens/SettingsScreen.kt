package com.example.fyp_androidapp.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fyp_androidapp.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    var userEmail by remember { mutableStateOf<String?>(auth.currentUser?.email) }
    var userName by remember { mutableStateOf<String?>(auth.currentUser?.displayName) }

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("410405106281-k82mf5kndd5e3vs1u01gg9hiihq8pe47.apps.googleusercontent.com")
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/calendar.events")) // Request Calendar Access
            .requestServerAuthCode("410405106281-k82mf5kndd5e3vs1u01gg9hiihq8pe47.apps.googleusercontent.com", true) // Request auth code
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val email = user?.email
                    val displayName = user?.displayName

                    userEmail = email
                    userName = displayName

                    // Send token to backend with all required parameters
                    sendTokenToBackend(user!!.uid, email, displayName, account.idToken!!)
                }
            }

        } catch (e: ApiException) {
            Log.w("GoogleSignIn", "Google sign-in failed", e)
        }
    }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            userEmail = user.email
            userName = user.displayName
        }
    }

    Scaffold(topBar = { CustomTopBar(title = "Manage Accounts") }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Accounts", fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (userEmail != null) {
                        AccountItem(
                            accountType = "Google Account",
                            accountEmail = userEmail!!,
                            onUnlinkClick = {
                                auth.signOut()
                                googleSignInClient.signOut().addOnCompleteListener {
                                    userEmail = null
                                    userName = null
                                }
                            }
                        )
                        Divider(
                            color = Color.Black,
                            thickness = 1.dp,
                            modifier = Modifier.padding(start = 72.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (userEmail == null) {
                Button(
                    onClick = { signInLauncher.launch(googleSignInClient.signInIntent) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sign in with Google")
                }
            } else {
                Button(
                    onClick = {
                        auth.signOut()
                        googleSignInClient.signOut().addOnCompleteListener {
                            userEmail = null
                            userName = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sign Out")
                }
            }
        }
    }
}


@Composable
fun CustomTopBar(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

@Composable
fun AccountItem(accountType: String, accountEmail: String, onUnlinkClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Gray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "L", // Placeholder for logo
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = accountType,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = accountEmail,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .clickable { onUnlinkClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Unlink Calendar", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}


fun sendTokenToBackend(userId: String, email: String?, displayName: String?, idToken: String?) {
    if (idToken == null) {
        Log.e("sendTokenToBackend", "ID Token is null. Aborting request.")
        return
    }

    val client = OkHttpClient()
    val requestBody = JSONObject().apply {
        put("userId", userId)
        put("email", email ?: "Unknown")
        put("displayName", displayName ?: "Unknown")
        put("idToken", idToken)
    }.toString().toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("${Constants.BASE_URL}/users/save-user") // ✅ Correct API route
        .post(requestBody)
        .build()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("sendTokenToBackend", "Failed to send token: ${response.code} - ${response.message}")
                } else {
                    Log.d("sendTokenToBackend", "Token sent successfully: ${response.code}")
                }
            }
        } catch (e: Exception) {
            Log.e("sendTokenToBackend", "Error sending token to backend", e)
        }
    }
}

