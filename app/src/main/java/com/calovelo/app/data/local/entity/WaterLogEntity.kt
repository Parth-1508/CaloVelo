package com.calovelo.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "water_log",
    indices = [Index(value = ["timestamp"])]
)
data class WaterLogEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String = UUID.randomUUID().toString(),
    /** Epoch milliseconds (UTC). */
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "amount_ml") val amountMl: Int
)
