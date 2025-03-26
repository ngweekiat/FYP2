package com.example.fyp_androidapp

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.fyp_androidapp.ui.theme.FYP_AndroidAppTheme
import com.example.fyp_androidapp.viewmodel.AuthViewModel
import android.content.Context
import android.service.notification.NotificationListenerService
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import com.example.fyp_androidapp.data.repository.AuthRepository
import com.example.fyp_androidapp.database.DatabaseProvider
import com.example.fyp_androidapp.ui.MainAppContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initially room database globally
        DatabaseProvider.init(applicationContext)


        // Check if Notification Listener Permission is granted
        if (!isNotificationServiceEnabled()) {
            Toast.makeText(this, "Please enable notification access", Toast.LENGTH_LONG).show()
            openNotificationAccessSettings()
        }

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
            val userDao = DatabaseProvider.getDatabase().userDao()
            val authRepository = AuthRepository(userDao)
            val authViewModel = AuthViewModel(authRepository)
            FYP_AndroidAppTheme {
                MainAppContent(authViewModel)
            }
        }
    }

    // Function to check if Notification Listener Service is enabled
    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return !TextUtils.isEmpty(enabledListeners) && enabledListeners.contains(pkgName)
    }

    // Function to open the settings page to enable Notification Access
    private fun openNotificationAccessSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
