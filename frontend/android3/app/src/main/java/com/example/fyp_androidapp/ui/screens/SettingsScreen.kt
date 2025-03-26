package com.example.fyp_androidapp.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fyp_androidapp.ui.components.AccountItem
import com.example.fyp_androidapp.ui.components.CustomTopBar
import com.example.fyp_androidapp.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AuthViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadPersistedUsers()
    }

    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()

    val googleSignInClient = remember {
        viewModel.getGoogleSignInClient(context)
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val uid = account.id ?: account.email ?: account.displayName ?: "unknown"
            viewModel.signInWithGoogle(
                uid = uid,
                email = account.email,
                displayName = account.displayName,
                idToken = account.idToken,
                authCode = account.serverAuthCode
            )        } catch (e: ApiException) {
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
                            userPhotoUrl = null,
                            onUnlinkClick = { viewModel.signOut(index) }
                        )
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
