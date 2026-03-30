package com.motherledisa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database for MotherLEDisa.
 *
 * Schema:
 * - tower_configs: Device identification, user preferences, ordering
 * - animations: Custom animation presets (added in v2)
 */
@Database(
    entities = [TowerConfigEntity::class, AnimationEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(KeyframeListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    /** DAO for tower configuration operations */
    abstract fun towerConfigDao(): TowerConfigDao

    /** DAO for animation CRUD operations */
    abstract fun animationDao(): AnimationDao

    companion object {
        /** Migration from v1 to v2: add animations table */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS animations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        durationMs INTEGER NOT NULL,
                        keyframesJson TEXT NOT NULL,
                        loopMode TEXT NOT NULL DEFAULT 'ONCE',
                        loopCount INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL
                    )
                """)
            }
        }
    }
}
