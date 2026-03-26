package com.motherledisa.di

import android.content.Context
import androidx.room.Room
import com.motherledisa.data.local.AppDatabase
import com.motherledisa.data.local.TowerConfigDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-wide dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the Room database instance.
     * Singleton - single database for entire app lifecycle.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "motherledisa_database"
        )
            // Allow destructive migration for now (MVP)
            // TODO: Add proper migrations before release
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the TowerConfigDao from the database.
     */
    @Provides
    @Singleton
    fun provideTowerConfigDao(database: AppDatabase): TowerConfigDao {
        return database.towerConfigDao()
    }
}
