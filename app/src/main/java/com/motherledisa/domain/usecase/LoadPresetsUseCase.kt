package com.motherledisa.domain.usecase

import com.motherledisa.data.repository.AnimationRepository
import com.motherledisa.domain.model.Animation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for loading saved presets.
 * Per PRESET-02: User can view list of saved presets.
 * Per PRESET-03: User can apply saved preset to connected tower(s).
 */
class LoadPresetsUseCase @Inject constructor(
    private val repository: AnimationRepository
) {
    /**
     * Get all saved presets as a Flow.
     * Per PRESET-02: View list of saved presets.
     */
    operator fun invoke(): Flow<List<Animation>> {
        return repository.getAllAnimations()
    }

    /**
     * Get a specific preset by ID.
     * Per PRESET-03: Load preset for applying.
     */
    suspend fun getById(id: Long): Animation? {
        return repository.getById(id)
    }
}
