package com.cathub

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Cat Hub Application.
 * Uses Hilt for dependency injection.
 */
@HiltAndroidApp
class CatHubApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // TODO: Initialize services
        // - Porcupine wake word
        // - WebSocket relay connection
        // - Calendar sync
    }
}
