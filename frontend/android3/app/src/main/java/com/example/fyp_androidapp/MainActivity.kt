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
import com.example.fyp_androidapp.data.repository.AuthRepository
import com.example.fyp_androidapp.database.DatabaseProvider
import com.example.fyp_androidapp.ui.MainAppContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Room database globally
        DatabaseProvider.init(applicationContext)

        // Prompt for notification listener permission
        if (!isNotificationServiceEnabled()) {
            Toast.makeText(this, "Please enable notification access", Toast.LENGTH_LONG).show()
            openNotificationAccessSettings()
        }

        // Full screen setup
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )

        // Set main content
        setContent {
            val userDao = DatabaseProvider.getDatabase().userDao()
            val authRepository = AuthRepository(userDao)
            val authViewModel = AuthViewModel(authRepository)

            FYP_AndroidAppTheme {
                MainAppContent(authViewModel)
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return !enabledListeners.isNullOrEmpty() && enabledListeners.contains(pkgName)
    }

    private fun openNotificationAccessSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
