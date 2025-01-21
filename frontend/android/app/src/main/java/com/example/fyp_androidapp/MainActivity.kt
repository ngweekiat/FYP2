package com.example.fyp_androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fyp_androidapp.ui.components.BottomTabBar
import com.example.fyp_androidapp.ui.screens.*
import com.example.fyp_androidapp.ui.theme.FYP_AndroidAppTheme
import com.example.fyp_androidapp.data.models.TableItem
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import android.view.View

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable immersive mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )

        setContent {
            FYP_AndroidAppTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController: NavHostController = rememberNavController()

    // Define tab items
    val tabs = listOf(
        TableItem("Notifications", Icons.Default.Notifications, "notifications"),
        TableItem("Events", Icons.Default.Event, "events"),
        TableItem("Calendar", Icons.Default.CalendarToday, "calendar"),
        TableItem("Settings", Icons.Default.Settings, "settings")
    )

    Scaffold(
        bottomBar = {
            BottomTabBar(
                tabs = tabs,
                currentRoute = navController.currentBackStackEntry?.destination?.route ?: "",
                onTabSelected = { route -> navController.navigate(route) }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "notifications",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("notifications") { NotificationsScreen() }
            composable("events") { EventsScreen() }
            composable("calendar") { CalendarScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    FYP_AndroidAppTheme {
        MainApp()
    }
}
