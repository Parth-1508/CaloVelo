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

    @Query("DELETE FROM food_catalog")
    suspend fun deleteAll()

    @Query("SELECT * FROM food_catalog WHERE label_key = :labelKey LIMIT 1")
    suspend fun getByLabelKey(labelKey: String): FoodCatalogEntity?

    @Query("SELECT * FROM food_catalog WHERE label_key IN (:labelKeys)")
    suspend fun getByLabelKeys(labelKeys: List<String>): List<FoodCatalogEntity>

    @Query("SELECT * FROM food_catalog ORDER BY display_name COLLATE NOCASE ASC")
    suspend fun getAll(): List<FoodCatalogEntity>

    @Query("SELECT * FROM food_catalog ORDER BY display_name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<FoodCatalogEntity>>

    @Query(
        "SELECT * FROM food_catalog WHERE " +
            "(:isVegOnly = 0 OR (is_veg = 1 AND is_non_veg = 0)) AND " +
            "(:isVeganOnly = 0 OR is_vegan = 1) AND " +
            "(:isEggOnly = 0 OR ((is_veg = 1 OR is_egg = 1) AND is_non_veg = 0)) AND " +
            "(:isLactoseFreeOnly = 0 OR contains_lactose = 0) " +
            "ORDER BY display_name COLLATE NOCASE ASC"
    )
    suspend fun getAllFiltered(
        isVegOnly: Int,
        isVeganOnly: Int,
        isEggOnly: Int,
        isLactoseFreeOnly: Int
    ): List<FoodCatalogEntity>

    /**
     * Substring search on name, category, or label key, combined with token matching and dietary filters.
     */
    @Query(
        "SELECT * FROM food_catalog WHERE " +
            "(" +
            "  LOWER(display_name) LIKE '%' || LOWER(:query) || '%' " +
            "  OR LOWER(category) LIKE '%' || LOWER(:query) || '%' " +
            "  OR LOWER(label_key) LIKE '%' || LOWER(:query) || '%' " +
            "  OR (" +
            "    (display_name LIKE :token1 ESCAPE '\\' OR category LIKE :token1 ESCAPE '\\') AND " +
            "    (display_name LIKE :token2 ESCAPE '\\' OR category LIKE :token2 ESCAPE '\\') AND " +
            "    (display_name LIKE :token3 ESCAPE '\\' OR category LIKE :token3 ESCAPE '\\')" +
            "  )" +
            ") AND " +
            "(:isVegOnly = 0 OR (is_veg = 1 AND is_non_veg = 0)) AND " +
            "(:isVeganOnly = 0 OR is_vegan = 1) AND " +
            "(:isEggOnly = 0 OR ((is_veg = 1 OR is_egg = 1) AND is_non_veg = 0)) AND " +
            "(:isLactoseFreeOnly = 0 OR contains_lactose = 0) " +
            "ORDER BY CASE WHEN LOWER(display_name) LIKE LOWER(:query) || '%' THEN 0 ELSE 1 END, " +
            "display_name COLLATE NOCASE ASC " +
            "LIMIT :limit"
    )
    suspend fun searchFiltered(
        query: String,
        token1: String,
        token2: String,
        token3: String,
        isVegOnly: Int,
        isVeganOnly: Int,
        isEggOnly: Int,
        isLactoseFreeOnly: Int,
        limit: Int
    ): List<FoodCatalogEntity>

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
