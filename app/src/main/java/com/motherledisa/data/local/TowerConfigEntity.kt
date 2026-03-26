package com.motherledisa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing tower device configurations.
 * Persists device identification and user preferences.
 */
@Entity(tableName = "tower_configs")
data class TowerConfigEntity(
    /** MAC address - unique device identifier */
    @PrimaryKey
    val address: String,

    /** Original BLE advertisement name (e.g., "MELK-12AB34") */
    val deviceName: String,

    /** User-assigned custom name (D-16: e.g., "Desk Tower") */
    val customName: String? = null,

    /** Timestamp of last successful connection (for "Last connected" badge - D-05) */
    val lastConnected: Long? = null,

    /** Tower ordering position for Phase 4 multi-tower orchestration */
    val position: Int? = null
)
