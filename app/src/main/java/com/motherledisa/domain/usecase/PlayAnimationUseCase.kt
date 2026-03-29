package com.motherledisa.domain.usecase

import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.domain.animation.AnimationPlayer
import com.motherledisa.domain.model.Animation
import javax.inject.Inject

/**
 * Use case for playing animations on tower(s).
 * Per ANIM-06: User can play animation on connected tower(s).
 */
class PlayAnimationUseCase @Inject constructor(
    private val animationPlayer: AnimationPlayer
) {
    /**
     * Play animation on a specific tower.
     */
    operator fun invoke(animation: Animation, tower: ConnectedTower) {
        animationPlayer.play(animation, tower)
    }

    /**
     * Play animation on all connected towers.
     * Per D-16: "Apply" sends preset animation to currently selected device(s).
     */
    fun invokeAll(animation: Animation) {
        animationPlayer.play(animation, null)
    }

    /**
     * Pause current playback.
     * Per ANIM-07: User can pause animation playback.
     */
    fun pause() {
        animationPlayer.pause()
    }

    /**
     * Resume paused playback.
     */
    fun resume() {
        animationPlayer.resume()
    }

    /**
     * Stop playback.
     * Per ANIM-07: User can stop animation playback.
     */
    fun stop() {
        animationPlayer.stop()
    }
}
