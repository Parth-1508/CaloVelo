package com.calovelo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.calovelo.app.data.local.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {

    @Upsert
    suspend fun upsert(log: WaterLogEntity)

    @Query("DELETE FROM water_log WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query(
        "SELECT * FROM water_log WHERE timestamp >= :startMillis AND timestamp < :endMillis " +
            "ORDER BY timestamp DESC"
    )
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<WaterLogEntity>>

    @Query(
        "SELECT COALESCE(SUM(amount_ml), 0) FROM water_log " +
            "WHERE timestamp >= :startMillis AND timestamp < :endMillis"
    )
    fun observeTotalBetween(startMillis: Long, endMillis: Long): Flow<Int>

    @Query(
        "SELECT COALESCE(SUM(amount_ml), 0) FROM water_log " +
            "WHERE timestamp >= :startMillis AND timestamp < :endMillis"
    )
    suspend fun getTotalBetween(startMillis: Long, endMillis: Long): Int
}
