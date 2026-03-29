package com.motherledisa.data.repository

import com.motherledisa.data.local.AnimationDao
import com.motherledisa.data.local.AnimationEntity
import com.motherledisa.data.local.KeyframeListConverter
import com.motherledisa.domain.model.Animation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for animation persistence.
 * Handles domain<->entity conversion and provides Flow-based queries.
 */
@Singleton
class AnimationRepository @Inject constructor(
    private val animationDao: AnimationDao,
    private val keyframeConverter: KeyframeListConverter
) {
    /** Get all animations as Flow of domain models */
    fun getAllAnimations(): Flow<List<Animation>> =
        animationDao.getAllAnimations().map { entities ->
            entities.map { entity ->
                val keyframes = keyframeConverter.fromJson(entity.keyframesJson)
                entity.toDomain(keyframes)
            }
        }

    /** Get single animation by ID */
    suspend fun getById(id: Long): Animation? {
        val entity = animationDao.getById(id) ?: return null
        val keyframes = keyframeConverter.fromJson(entity.keyframesJson)
        return entity.toDomain(keyframes)
    }

    /** Save animation (insert or update) */
    suspend fun save(animation: Animation): Long {
        val keyframesJson = keyframeConverter.toJson(animation.keyframes)
        val entity = AnimationEntity.fromDomain(animation, keyframesJson)
        return if (animation.id == 0L) {
            animationDao.insert(entity)
        } else {
            animationDao.update(entity)
            animation.id
        }
    }

    /** Delete animation */
    suspend fun delete(animation: Animation) {
        animationDao.deleteById(animation.id)
    }

    /** Delete animation by ID */
    suspend fun deleteById(id: Long) {
        animationDao.deleteById(id)
    }
}
