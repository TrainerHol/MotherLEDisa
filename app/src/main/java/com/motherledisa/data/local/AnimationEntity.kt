package com.motherledisa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.motherledisa.domain.model.Animation
import com.motherledisa.domain.model.Keyframe
import com.motherledisa.domain.model.LoopMode

/**
 * Room entity for persisting animations.
 * Per D-15: Presets stored in Room with JSON-serialized keyframe data.
 */
@Entity(tableName = "animations")
data class AnimationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val durationMs: Long,
    val keyframesJson: String,  // JSON-serialized List<Keyframe>
    val loopMode: String = LoopMode.ONCE.name,
    val loopCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Convert to domain model */
    fun toDomain(keyframes: List<Keyframe>): Animation = Animation(
        id = id,
        name = name,
        durationMs = durationMs,
        keyframes = keyframes,
        loopMode = LoopMode.valueOf(loopMode),
        loopCount = loopCount,
        createdAt = createdAt
    )

    companion object {
        /** Convert from domain model */
        fun fromDomain(animation: Animation, keyframesJson: String): AnimationEntity =
            AnimationEntity(
                id = animation.id,
                name = animation.name,
                durationMs = animation.durationMs,
                keyframesJson = keyframesJson,
                loopMode = animation.loopMode.name,
                loopCount = animation.loopCount,
                createdAt = animation.createdAt
            )
    }
}
