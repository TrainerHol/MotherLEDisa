package com.motherledisa

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Application class for MotherLEDisa.
 * Initializes Hilt dependency injection and logging.
 */
@HiltAndroidApp
class MotherLEDisaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Always plant a Timber tree so we get logging in release too
        Timber.plant(Timber.DebugTree())

        // Install crash handler that writes stack trace to a file
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val crashLog = sw.toString()
                Log.e("MotherLEDisa", "CRASH: $crashLog")
                val file = File(getExternalFilesDir(null), "crash_log.txt")
                file.writeText("Thread: ${thread.name}\n$crashLog")
            } catch (_: Exception) {
                // Don't crash the crash handler
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }

        Timber.d("MotherLEDisaApp initialized")
    }
}
