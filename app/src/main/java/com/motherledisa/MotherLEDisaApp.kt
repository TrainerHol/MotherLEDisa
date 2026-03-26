package com.motherledisa

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for MotherLEDisa.
 * Initializes Hilt dependency injection and logging.
 */
@HiltAndroidApp
class MotherLEDisaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber logging for debug builds
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("MotherLEDisaApp initialized")
        }
    }
}
