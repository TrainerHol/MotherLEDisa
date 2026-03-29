package com.motherledisa.domain.usecase

import com.motherledisa.data.repository.AnimationRepository
import com.motherledisa.domain.model.Animation
import javax.inject.Inject

/**
 * Use case for deleting saved presets.
 * Per PRESET-04: User can delete saved presets.
 */
class DeletePresetUseCase @Inject constructor(
    private val repository: AnimationRepository
) {
    /**
     * Delete a preset.
     */
    suspend operator fun invoke(animation: Animation) {
        repository.delete(animation)
    }

    /**
     * Delete a preset by ID.
     */
    suspend fun byId(id: Long) {
        repository.deleteById(id)
    }
}
