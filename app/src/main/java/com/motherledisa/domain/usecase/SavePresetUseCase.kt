package com.motherledisa.domain.usecase

import com.motherledisa.data.repository.AnimationRepository
import com.motherledisa.domain.model.Animation
import javax.inject.Inject

/**
 * Use case for saving animations as presets.
 * Per PRESET-01: User can save current animation as named preset.
 */
class SavePresetUseCase @Inject constructor(
    private val repository: AnimationRepository
) {
    /**
     * Save animation with a name.
     * @param animation The animation to save
     * @param name Name for the preset
     * @return ID of the saved animation
     */
    suspend operator fun invoke(animation: Animation, name: String): Long {
        val namedAnimation = animation.copy(
            name = name,
            createdAt = System.currentTimeMillis()
        )
        return repository.save(namedAnimation)
    }

    /**
     * Save animation using its existing name.
     * @param animation The animation to save
     * @return ID of the saved animation
     */
    suspend operator fun invoke(animation: Animation): Long {
        return repository.save(animation)
    }
}
