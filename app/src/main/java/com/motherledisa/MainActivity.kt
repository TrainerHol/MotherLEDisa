package com.motherledisa

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.motherledisa.ui.navigation.NavGraph
import com.motherledisa.ui.theme.MotherLEDisaTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Main entry point for MotherLEDisa.
 * Handles BLE permission requests, starts the foreground service, and hosts the Compose UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Whether permissions have been granted */
    private var permissionsGranted by mutableStateOf(false)

    /** Permission request launcher */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        permissionsGranted = allGranted
        if (allGranted) {
            Timber.d("All BLE permissions granted")
        } else {
            Timber.w("Some BLE permissions denied: ${permissions.filter { !it.value }.keys}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check and request BLE permissions
        checkAndRequestPermissions()

        setContent {
            MotherLEDisaTheme {
                NavGraph()
            }
        }
    }

    /**
     * Checks BLE permissions and requests them if not granted.
     * Android 12+ requires BLUETOOTH_SCAN and BLUETOOTH_CONNECT.
     * Older versions require ACCESS_FINE_LOCATION.
     */
    private fun checkAndRequestPermissions() {
        val requiredPermissions = getRequiredPermissions()

        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            Timber.d("All BLE permissions already granted")
            permissionsGranted = true
        } else {
            Timber.d("Requesting BLE permissions: $missingPermissions")
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    /**
     * Returns the required permissions based on Android version.
     * Android 12+ (API 31+): BLUETOOTH_SCAN, BLUETOOTH_CONNECT
     * Android 10-11 (API 29-30): ACCESS_FINE_LOCATION
     */
    private fun getRequiredPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+)
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            // Android 10-11 (API 29-30)
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

}
