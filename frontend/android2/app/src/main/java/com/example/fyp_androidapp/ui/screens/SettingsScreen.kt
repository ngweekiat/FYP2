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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fyp_androidapp.ui.components.AccountItem
import com.example.fyp_androidapp.ui.components.CustomTopBar
import com.example.fyp_androidapp.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AuthViewModel = viewModel()) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("410405106281-k82mf5kndd5e3vs1u01gg9hiihq8pe47.apps.googleusercontent.com")
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/calendar.events"))
            .requestServerAuthCode("410405106281-k82mf5kndd5e3vs1u01gg9hiihq8pe47.apps.googleusercontent.com", true)
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            viewModel.signInWithGoogle(account.idToken, account.serverAuthCode)
        } catch (e: ApiException) {
            Log.w("GoogleSignIn", "Google sign-in failed", e)
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
                    accounts.forEachIndexed { index, user ->
                        AccountItem(
                            accountType = "Google Account",
                            accountEmail = user.email ?: "Unknown Email",
                            userPhotoUrl = user.photoUrl?.toString(),
                            onUnlinkClick = { viewModel.signOut(index) }
                        )
                        if (index != accounts.lastIndex) {
                            Divider(
                                color = Color.Black,
                                thickness = 1.dp,
                                modifier = Modifier.padding(start = 72.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { signInLauncher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Link Account")
            }
        }
    }
}
