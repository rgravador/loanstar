package com.example.loanstar

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Loanstar app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class LoanstarApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide components here if needed
    }
}
