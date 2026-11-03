package com.calovelo.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.calovelo.app.data.local.dao.HealthSummaryDao
import com.calovelo.app.data.local.dao.WaterLogDao
import com.calovelo.app.data.local.dao.WorkoutSessionDao
import com.calovelo.app.data.local.entity.DailyHealthSummaryEntity
import com.calovelo.app.data.local.entity.WaterLogEntity
import com.calovelo.app.data.local.entity.WorkoutSessionEntity

@Database(
    entities = [
        DailyHealthSummaryEntity::class,
        WaterLogEntity::class,
        WorkoutSessionEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class CaloveloDatabase : RoomDatabase() {
    abstract fun healthSummaryDao(): HealthSummaryDao
    abstract fun waterLogDao(): WaterLogDao
    abstract fun workoutSessionDao(): WorkoutSessionDao

    companion object {
        const val DATABASE_NAME = "calovelo_db"

        /** v1 -> v2: adds the workout_session table (Phase 2). Existing data is preserved. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `workout_session` (" +
                        "`id` TEXT NOT NULL, " +
                        "`source` TEXT NOT NULL, " +
                        "`activity_type` TEXT NOT NULL, " +
                        "`start_time` INTEGER NOT NULL, " +
                        "`end_time` INTEGER NOT NULL, " +
                        "`duration_seconds` INTEGER NOT NULL, " +
                        "`distance_meters` REAL NOT NULL, " +
                        "`calories_burned` INTEGER NOT NULL, " +
                        "`avg_heart_rate` INTEGER, " +
                        "`route_polyline` TEXT, " +
                        "`rpe_score` INTEGER, " +
                        "`feeling_tag` TEXT, " +
                        "`user_notes` TEXT, " +
                        "`is_shared_to_club` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_workout_session_start_time` " +
                        "ON `workout_session` (`start_time`)"
                )
            }
        }
    }
}
