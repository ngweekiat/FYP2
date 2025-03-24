package com.example.fyp_androidapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fyp_androidapp.ui.components.BottomTabBar
import com.example.fyp_androidapp.ui.screens.*
import com.example.fyp_androidapp.data.models.TableItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.fyp_androidapp.viewmodel.AuthViewModel
import com.example.fyp_androidapp.viewmodel.NotificationsViewModel


@Composable
fun MainApp(authViewModel: AuthViewModel) {
    val navController: NavHostController = rememberNavController()

    Scaffold(
        bottomBar = {
            // Hide BottomTabBar on splash and login screens
            if (navController.currentBackStackEntry?.destination?.route !in listOf("splash", "login")) {
                BottomTabBar(
                    tabs = listOf(
                        TableItem("Notifications", Icons.Default.Notifications, "notifications"),
                        TableItem("Calendar", Icons.Default.CalendarToday, "calendar"),
                        TableItem("Settings", Icons.Default.Settings, "settings")
                    ),
                    currentRoute = navController.currentBackStackEntry?.destination?.route ?: "",
                    onTabSelected = { navController.navigate(it) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash", // Start from SplashScreen
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") { SplashScreen(navController, authViewModel) }
            composable("login") { LoginScreen(navController, authViewModel) }
            composable("notifications") { NotificationsScreen() }
            composable("calendar") { CalendarScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

