package com.motherledisa.data.local

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.motherledisa.domain.model.Keyframe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Room TypeConverter for JSON keyframe serialization.
 * Uses Kotlinx Serialization per RESEARCH.md Pattern 3.
 */
@ProvidedTypeConverter
class KeyframeListConverter @Inject constructor(
    private val json: Json
) {
    @TypeConverter
    fun fromJson(value: String): List<Keyframe> =
        if (value.isEmpty()) emptyList()
        else json.decodeFromString(value)

    @TypeConverter
    fun toJson(keyframes: List<Keyframe>): String =
        json.encodeToString(keyframes)
}
