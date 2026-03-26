package com.motherledisa

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.motherledisa.data.ble.BleConnectionService
import com.motherledisa.ui.theme.MotherLEDisaTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Main entry point for MotherLEDisa.
 * Starts the BLE foreground service and hosts the Compose UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start foreground service for BLE operations
        startBleService()

        setContent {
            MotherLEDisaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // TODO: Replace with NavHost in Phase 1 Plan 02
                    Text(text = "MotherLEDisa - BLE Foundation Complete")
                }
            }
        }
    }

    /**
     * Starts the BLE connection foreground service.
     * Required for background BLE stability (D-14).
     */
    private fun startBleService() {
        val serviceIntent = Intent(this, BleConnectionService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        Timber.d("BLE foreground service started")
    }
}
