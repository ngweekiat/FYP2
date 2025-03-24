package com.example.fyp_androidapp

import android.app.Application
import com.example.fyp_androidapp.database.DatabaseProvider

class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseProvider.init(applicationContext)
    }
}
