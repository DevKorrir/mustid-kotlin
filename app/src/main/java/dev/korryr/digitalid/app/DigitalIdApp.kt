package dev.korryr.digitalid.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DigitalIdApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Optional: Add any app-wide initialization here
    }
}