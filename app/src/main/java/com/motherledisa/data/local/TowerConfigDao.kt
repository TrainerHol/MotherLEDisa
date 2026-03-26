package com.motherledisa.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for tower configuration persistence.
 * Provides reactive queries via Flow for UI updates.
 */
@Dao
interface TowerConfigDao {

    /**
     * Gets all known devices, ordered by most recently connected.
     * Used for merging with scan results (D-05: known devices in list).
     */
    @Query("SELECT * FROM tower_configs ORDER BY lastConnected DESC")
    fun getAllKnownDevices(): Flow<List<TowerConfigEntity>>

    /**
     * Gets a single device by MAC address.
     */
    @Query("SELECT * FROM tower_configs WHERE address = :address")
    suspend fun getByAddress(address: String): TowerConfigEntity?

    /**
     * Gets all devices ordered by user-defined position (for Phase 4).
     */
    @Query("SELECT * FROM tower_configs WHERE position IS NOT NULL ORDER BY position ASC")
    fun getOrderedDevices(): Flow<List<TowerConfigEntity>>

    /**
     * Inserts or updates a device configuration.
     * Uses REPLACE strategy for upsert behavior.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: TowerConfigEntity)

    /**
     * Updates the last connected timestamp for a device.
     * Called when connection is established.
     */
    @Query("UPDATE tower_configs SET lastConnected = :timestamp WHERE address = :address")
    suspend fun updateLastConnected(address: String, timestamp: Long)

    /**
     * Updates the custom display name for a device (D-16).
     */
    @Query("UPDATE tower_configs SET customName = :name WHERE address = :address")
    suspend fun updateCustomName(address: String, name: String)

    /**
     * Clears the custom name (reverts to device name).
     */
    @Query("UPDATE tower_configs SET customName = NULL WHERE address = :address")
    suspend fun clearCustomName(address: String)

    /**
     * Updates the position for tower ordering (Phase 4).
     */
    @Query("UPDATE tower_configs SET position = :position WHERE address = :address")
    suspend fun updatePosition(address: String, position: Int)

    /**
     * Deletes a device configuration.
     */
    @Delete
    suspend fun delete(config: TowerConfigEntity)

    /**
     * Deletes all device configurations.
     */
    @Query("DELETE FROM tower_configs")
    suspend fun deleteAll()
}
