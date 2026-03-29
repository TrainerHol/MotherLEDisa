package com.motherledisa.di

import com.motherledisa.data.ble.TowerConnectionManager
import com.motherledisa.domain.animation.AnimationEvaluator
import com.motherledisa.domain.animation.AnimationPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for animation-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AnimationModule {

    @Provides
    @Singleton
    fun provideAnimationEvaluator(): AnimationEvaluator = AnimationEvaluator()

    @Provides
    @Singleton
    fun provideAnimationPlayer(
        connectionManager: TowerConnectionManager,
        evaluator: AnimationEvaluator
    ): AnimationPlayer = AnimationPlayer(connectionManager, evaluator)
}
