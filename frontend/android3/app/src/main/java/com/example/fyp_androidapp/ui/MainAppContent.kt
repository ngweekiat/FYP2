package com.example.fyp_androidapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fyp_androidapp.ui.screens.TextImageInputScreen
import com.example.fyp_androidapp.data.models.TableItem
import com.example.fyp_androidapp.data.repository.GoogleCalendarApiRepository
import com.example.fyp_androidapp.database.DatabaseProvider
import com.example.fyp_androidapp.ui.components.BottomTabBar
import com.example.fyp_androidapp.ui.screens.*
import com.example.fyp_androidapp.viewmodel.AuthViewModel
import com.example.fyp_androidapp.viewmodel.NotificationsViewModel
import com.example.fyp_androidapp.data.repository.CalendarRepository
import com.example.fyp_androidapp.viewmodel.CalendarViewModel

@Composable
fun MainAppContent(authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val userDao = remember { DatabaseProvider.getDatabase().userDao() }

    // ✅ Access AuthRepository from AuthViewModel
    val authRepository = authViewModel.authRepository

    // ✅ Pass both userDao and authRepository
    val googleCalendarApiRepository = remember {
        GoogleCalendarApiRepository(userDao, authRepository)
    }

    val notificationsViewModel = remember {
        NotificationsViewModel(googleCalendarApiRepository = googleCalendarApiRepository)
    }

    val calendarRepository = remember { CalendarRepository() }
    val calendarViewModel = remember {
        CalendarViewModel(
            userId = "currentUser",
            calendarRepository = calendarRepository,
            googleCalendarApiRepository = googleCalendarApiRepository
        )
    }

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            if (navController.currentBackStackEntry?.destination?.route !in listOf("splash", "login")) {
                BottomTabBar(
                    tabs = listOf(
                        TableItem("Notifications", Icons.Default.Notifications, "notifications"),
                        TableItem("Text/Image", Icons.Default.Add, "textimage"),
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
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") { SplashScreen(navController, authViewModel) }
            composable("login") { LoginScreen(navController, authViewModel) }
            composable("notifications") { NotificationsScreen(notificationsViewModel) }
            composable("textimage") { TextImageInputScreen(notificationsViewModel) }
            composable("calendar") { CalendarScreen(viewModel = calendarViewModel) }
            composable("settings") { SettingsScreen(authViewModel) }
        }
    }
}
