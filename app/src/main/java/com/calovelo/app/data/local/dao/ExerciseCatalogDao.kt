// app/src/main/java/com/calovelo/app/data/local/dao/ExerciseCatalogDao.kt
package com.calovelo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.calovelo.app.data.local.entity.ExerciseCatalogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseCatalogDao {

    /** Inserts new exercises and replaces existing ones with the same id, in one transaction. */
    @Upsert
    suspend fun upsertAll(exercises: List<ExerciseCatalogEntity>)

    @Query("SELECT COUNT(*) FROM exercise_catalog")
    suspend fun count(): Int

    @Query("SELECT * FROM exercise_catalog WHERE exercise_id = :exerciseId LIMIT 1")
    suspend fun getById(exerciseId: String): ExerciseCatalogEntity?

    @Query("SELECT * FROM exercise_catalog WHERE exercise_id IN (:exerciseIds)")
    suspend fun getByIds(exerciseIds: List<String>): List<ExerciseCatalogEntity>

    /** Distinct muscle-group keys present in the catalog, alphabetically. */
    @Query("SELECT DISTINCT muscle_group FROM exercise_catalog ORDER BY muscle_group ASC")
    suspend fun getMuscleGroups(): List<String>

    @Query(
        "SELECT * FROM exercise_catalog WHERE muscle_group = :muscleGroup " +
            "ORDER BY name COLLATE NOCASE ASC LIMIT :limit"
    )
    suspend fun filterByMuscleGroup(muscleGroup: String, limit: Int): List<ExerciseCatalogEntity>

    @Query("SELECT * FROM exercise_catalog WHERE muscle_group = :muscleGroup ORDER BY name COLLATE NOCASE ASC")
    fun observeByMuscleGroup(muscleGroup: String): Flow<List<ExerciseCatalogEntity>>

    /**
     * Substring search: every token must match the exercise's name, muscle group or equipment (AND
     * across tokens, so "bench barbell" and "barbell bench" both work). Pass `%` for unused tokens and
     * a [prefixPattern] (e.g. `bench%`) so names that start with the query rank first. Patterns must
     * be LIKE-escaped with `\` (see ExerciseSearchQuery). A null [muscleGroup] searches every group.
     * LIKE over a few thousand rows is instant and avoids an FTS shadow table that every migration
     * would have to keep in sync.
     */
    @Query(
        "SELECT * FROM exercise_catalog WHERE " +
            "(name LIKE :token1 ESCAPE '\\' OR muscle_group LIKE :token1 ESCAPE '\\' OR equipment_type LIKE :token1 ESCAPE '\\') AND " +
            "(name LIKE :token2 ESCAPE '\\' OR muscle_group LIKE :token2 ESCAPE '\\' OR equipment_type LIKE :token2 ESCAPE '\\') AND " +
            "(name LIKE :token3 ESCAPE '\\' OR muscle_group LIKE :token3 ESCAPE '\\' OR equipment_type LIKE :token3 ESCAPE '\\') AND " +
            "(:muscleGroup IS NULL OR muscle_group = :muscleGroup) " +
            "ORDER BY CASE WHEN name LIKE :prefixPattern ESCAPE '\\' THEN 0 ELSE 1 END, " +
            "name COLLATE NOCASE ASC " +
            "LIMIT :limit"
    )
    suspend fun search(
        token1: String,
        token2: String,
        token3: String,
        prefixPattern: String,
        muscleGroup: String?,
        limit: Int
    ): List<ExerciseCatalogEntity>
}
