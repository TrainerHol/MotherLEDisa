package com.motherledisa.di

import com.motherledisa.data.ble.TowerConnectionManager
import com.motherledisa.domain.animation.AnimationPlayer
import com.motherledisa.domain.orchestration.OrchestrationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing orchestration dependencies.
 * OrchestrationManager is @Singleton so this module ensures
 * consistent injection across ViewModels.
 */
@Module
@InstallIn(SingletonComponent::class)
object OrchestrationModule {

    @Provides
    @Singleton
    fun provideOrchestrationManager(
        connectionManager: TowerConnectionManager,
        animationPlayer: AnimationPlayer
    ): OrchestrationManager {
        return OrchestrationManager(connectionManager, animationPlayer)
    }
}
