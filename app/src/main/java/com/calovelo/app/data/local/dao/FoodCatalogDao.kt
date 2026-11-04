// app/src/main/java/com/calovelo/app/data/local/dao/FoodCatalogDao.kt
package com.calovelo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.calovelo.app.data.local.entity.FoodCatalogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodCatalogDao {

    /** Inserts new foods and replaces existing ones with the same label key. */
    @Upsert
    suspend fun upsertAll(foods: List<FoodCatalogEntity>)

    @Query("SELECT COUNT(*) FROM food_catalog")
    suspend fun count(): Int

    @Query("SELECT * FROM food_catalog WHERE label_key = :labelKey LIMIT 1")
    suspend fun getByLabelKey(labelKey: String): FoodCatalogEntity?

    @Query("SELECT * FROM food_catalog WHERE label_key IN (:labelKeys)")
    suspend fun getByLabelKeys(labelKeys: List<String>): List<FoodCatalogEntity>

    @Query("SELECT * FROM food_catalog ORDER BY display_name COLLATE NOCASE ASC")
    suspend fun getAll(): List<FoodCatalogEntity>

    @Query("SELECT * FROM food_catalog ORDER BY display_name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<FoodCatalogEntity>>

    /**
     * Substring search on name or category: every token must match one of the two (AND across
     * tokens, so "dal tad" and "tadka dal" both find Dal Tadka). Pass `%` for unused tokens; pass
     * [prefixPattern] (e.g. `dal%`) so names that *start* with the query rank first. Patterns must be
     * LIKE-escaped with `\` (see FoodSearchQuery). The catalog is small and seeded locally, so
     * LIKE is plenty and avoids maintaining an FTS shadow table across migrations.
     */
    @Query(
        "SELECT * FROM food_catalog WHERE " +
            "(display_name LIKE :token1 ESCAPE '\\' OR category LIKE :token1 ESCAPE '\\') AND " +
            "(display_name LIKE :token2 ESCAPE '\\' OR category LIKE :token2 ESCAPE '\\') AND " +
            "(display_name LIKE :token3 ESCAPE '\\' OR category LIKE :token3 ESCAPE '\\') " +
            "ORDER BY CASE WHEN display_name LIKE :prefixPattern ESCAPE '\\' THEN 0 ELSE 1 END, " +
            "display_name COLLATE NOCASE ASC " +
            "LIMIT :limit"
    )
    suspend fun search(
        token1: String,
        token2: String,
        token3: String,
        prefixPattern: String,
        limit: Int
    ): List<FoodCatalogEntity>
}
