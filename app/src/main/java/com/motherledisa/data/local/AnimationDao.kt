package com.motherledisa.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for animation CRUD operations.
 * Per PRESET-01 through PRESET-05.
 */
@Dao
interface AnimationDao {
    /** Get all animations as Flow (PRESET-02: view list) */
    @Query("SELECT * FROM animations ORDER BY createdAt DESC")
    fun getAllAnimations(): Flow<List<AnimationEntity>>

    /** Get single animation by ID */
    @Query("SELECT * FROM animations WHERE id = :id")
    suspend fun getById(id: Long): AnimationEntity?

    /** Insert new animation, returns new ID (PRESET-01: save) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(animation: AnimationEntity): Long

    /** Update existing animation */
    @Update
    suspend fun update(animation: AnimationEntity)

    /** Delete animation (PRESET-04: delete) */
    @Delete
    suspend fun delete(animation: AnimationEntity)

    /** Delete by ID */
    @Query("DELETE FROM animations WHERE id = :id")
    suspend fun deleteById(id: Long)
}
