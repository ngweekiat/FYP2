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


@Composable
fun MainApp() {
    val navController: NavHostController = rememberNavController()

    // Define tabs
    val tabs = listOf(
        TableItem("Notifications", Icons.Default.Notifications, "notifications"),
        TableItem("Calendar", Icons.Default.CalendarToday, "calendar"),
        TableItem("Settings", Icons.Default.Settings, "settings")
    )

    Scaffold(
        bottomBar = {
            BottomTabBar(
                tabs = tabs,
                currentRoute = navController.currentBackStackEntry?.destination?.route ?: "",
                onTabSelected = { navController.navigate(it) }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "notifications",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("notifications") { NotificationsScreen() }
            composable("calendar") { CalendarScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
