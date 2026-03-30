package com.motherledisa.data.local

import androidx.room.TypeConverter
import com.motherledisa.domain.model.Keyframe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room TypeConverter for JSON keyframe serialization.
 * Uses a static Json instance — no DI needed.
 */
class KeyframeListConverter {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromJson(value: String): List<Keyframe> =
        if (value.isEmpty()) emptyList()
        else json.decodeFromString(value)

    @TypeConverter
    fun toJson(keyframes: List<Keyframe>): String =
        json.encodeToString(keyframes)
}
