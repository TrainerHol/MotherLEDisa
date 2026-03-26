package com.motherledisa.data.ble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.motherledisa.MainActivity
import com.motherledisa.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Foreground service for maintaining BLE connections in background.
 *
 * Required for:
 * - Continuous scanning while app is backgrounded (D-01)
 * - Persistent auto-reconnect (D-13)
 * - Preventing Android Doze from killing BLE operations
 *
 * Reference: RESEARCH.md Pattern 3 - Foreground Service for BLE
 */
@AndroidEntryPoint
class BleConnectionService : LifecycleService() {

    @Inject
    lateinit var connectionManager: TowerConnectionManager

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "ble_connection"
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("BleConnectionService created")

        createNotificationChannel()
        startForegroundWithNotification(0)

        // Observe connected tower count and update notification
        lifecycleScope.launch {
            connectionManager.connectedTowers.collectLatest { towers ->
                updateNotification(towers.size)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.d("BleConnectionService started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("BleConnectionService destroyed")
    }

    /**
     * Creates the notification channel for Android 8+.
     * Uses IMPORTANCE_LOW for minimal user interruption (D-14).
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW // D-14: minimal notification
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        Timber.d("Notification channel created")
    }

    /**
     * Starts the foreground service with initial notification.
     */
    private fun startForegroundWithNotification(connectedCount: Int) {
        val notification = createNotification(connectedCount)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    /**
     * Creates the foreground notification.
     */
    private fun createNotification(connectedCount: Int): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentText = when (connectedCount) {
            0 -> getString(R.string.notification_no_towers)
            1 -> getString(R.string.notification_one_tower)
            else -> getString(R.string.notification_multiple_towers, connectedCount)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_led_notification)
            .setContentIntent(pendingIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    /**
     * Updates the notification with current connection count.
     */
    private fun updateNotification(connectedCount: Int) {
        val notification = createNotification(connectedCount)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
        Timber.d("Notification updated: $connectedCount towers connected")
    }
}
