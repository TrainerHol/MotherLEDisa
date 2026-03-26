package com.motherledisa.di

import android.content.Context
import com.motherledisa.data.ble.DeviceScanner
import com.motherledisa.data.ble.TowerConnectionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing BLE-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object BleModule {

    /**
     * Provides the DeviceScanner for BLE discovery.
     * Singleton - single scanner instance for app lifecycle.
     */
    @Provides
    @Singleton
    fun provideDeviceScanner(): DeviceScanner {
        return DeviceScanner()
    }

    /**
     * Provides the TowerConnectionManager for multi-device BLE connections.
     * Singleton - manages all active connections.
     */
    @Provides
    @Singleton
    fun provideTowerConnectionManager(
        @ApplicationContext context: Context
    ): TowerConnectionManager {
        return TowerConnectionManager(context)
    }
}
