package com.motherledisa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for MotherLEDisa.
 *
 * Schema:
 * - tower_configs: Device identification, user preferences, ordering
 *
 * Future tables (Phase 2+):
 * - animations: Custom animation presets
 * - audio_profiles: Sound reactivity configurations
 */
@Database(
    entities = [TowerConfigEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** DAO for tower configuration operations */
    abstract fun towerConfigDao(): TowerConfigDao
}
