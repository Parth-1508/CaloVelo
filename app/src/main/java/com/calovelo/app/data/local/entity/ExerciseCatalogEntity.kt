// app/src/main/java/com/calovelo/app/data/local/entity/ExerciseCatalogEntity.kt
package com.calovelo.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One exercise in the local gym catalog, seeded from assets/gym_seed.json. [muscleGroup] and
 * [equipmentType] hold the seed keys (MuscleGroup.key / EquipmentType.key).
 */
@Entity(
    tableName = "exercise_catalog",
    indices = [Index(value = ["name"]), Index(value = ["muscle_group"])]
)
data class ExerciseCatalogEntity(
    @PrimaryKey
    @ColumnInfo(name = "exercise_id") val exerciseId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "muscle_group") val muscleGroup: String,
    @ColumnInfo(name = "equipment_type") val equipmentType: String,
    @ColumnInfo(name = "instructions") val instructions: String
)
