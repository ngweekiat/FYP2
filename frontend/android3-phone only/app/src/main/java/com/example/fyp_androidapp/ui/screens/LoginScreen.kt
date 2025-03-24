package com.example.fyp_androidapp.ui.screens

import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fyp_androidapp.R
import com.example.fyp_androidapp.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope

@Composable
fun SplashScreen(navController: NavController, authViewModel: AuthViewModel) {
    LaunchedEffect(Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (authViewModel.accounts.value.isNotEmpty()) { // User is logged in
                navController.navigate("notifications") { // Navigate to the main app
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                navController.navigate("login") { // Navigate to login screen
                    popUpTo("splash") { inclusive = true }
                }
            }
        }, 2000) // 2-second delay
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = R.drawable.eventify_logo), contentDescription = "Eventify Logo")
            Spacer(modifier = Modifier.height(16.dp))
            Text("Eventify", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}


@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
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
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            authViewModel.signInWithGoogle(account.idToken, account.serverAuthCode)
            navController.navigate("notifications") { popUpTo("login") { inclusive = true } }
        } catch (e: ApiException) {
            println("Google sign-in failed: ${e.localizedMessage}")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome to Eventify", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { signInLauncher.launch(googleSignInClient.signInIntent) }) {
                Text("Sign in with Google")
            }
        }
    }
}