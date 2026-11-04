// app/src/main/java/com/calovelo/app/data/local/CaloveloDatabase.kt
package com.calovelo.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.calovelo.app.data.local.dao.ExerciseCatalogDao
import com.calovelo.app.data.local.dao.FoodCatalogDao
import com.calovelo.app.data.local.dao.GymWorkoutDao
import com.calovelo.app.data.local.dao.HealthSummaryDao
import com.calovelo.app.data.local.dao.MealLogDao
import com.calovelo.app.data.local.dao.WaterLogDao
import com.calovelo.app.data.local.dao.WorkoutSessionDao
import com.calovelo.app.data.local.entity.DailyHealthSummaryEntity
import com.calovelo.app.data.local.entity.ExerciseCatalogEntity
import com.calovelo.app.data.local.entity.ExerciseSetEntity
import com.calovelo.app.data.local.entity.FoodCatalogEntity
import com.calovelo.app.data.local.entity.GymWorkoutEntity
import com.calovelo.app.data.local.entity.MealLogEntity
import com.calovelo.app.data.local.entity.WaterLogEntity
import com.calovelo.app.data.local.entity.WorkoutSessionEntity

@Database(
    entities = [
        DailyHealthSummaryEntity::class,
        WaterLogEntity::class,
        WorkoutSessionEntity::class,
        FoodCatalogEntity::class,
        MealLogEntity::class,
        ExerciseCatalogEntity::class,
        GymWorkoutEntity::class,
        ExerciseSetEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class CaloveloDatabase : RoomDatabase() {
    abstract fun healthSummaryDao(): HealthSummaryDao
    abstract fun waterLogDao(): WaterLogDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun foodCatalogDao(): FoodCatalogDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun exerciseCatalogDao(): ExerciseCatalogDao
    abstract fun gymWorkoutDao(): GymWorkoutDao

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

        /**
         * v2 -> v3 (Phase 3): adds the food_catalog and meal_log tables. Existing data is untouched.
         * The catalog is created empty and filled from assets/nutrition_seed.json by FoodCatalogSeeder.
         * The DDL must match the Room-generated schema exactly (column types, NOT NULL, index names),
         * otherwise Room refuses to open the database.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `food_catalog` (" +
                        "`label_key` TEXT NOT NULL, " +
                        "`display_name` TEXT NOT NULL, " +
                        "`category` TEXT NOT NULL, " +
                        "`default_serving_grams` INTEGER NOT NULL, " +
                        "`kcal_per_100g` REAL NOT NULL, " +
                        "`protein_per_100g` REAL NOT NULL, " +
                        "`carbs_per_100g` REAL NOT NULL, " +
                        "`fat_per_100g` REAL NOT NULL, " +
                        "`fiber_per_100g` REAL NOT NULL, " +
                        "`suitable_meal_types` TEXT NOT NULL, " +
                        "`source` TEXT NOT NULL, " +
                        "PRIMARY KEY(`label_key`))"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_food_catalog_display_name` " +
                        "ON `food_catalog` (`display_name`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_food_catalog_category` " +
                        "ON `food_catalog` (`category`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `meal_log` (" +
                        "`id` TEXT NOT NULL, " +
                        "`food_label_key` TEXT NOT NULL, " +
                        "`food_name` TEXT NOT NULL, " +
                        "`meal_type` TEXT NOT NULL, " +
                        "`status` TEXT NOT NULL, " +
                        "`source` TEXT NOT NULL, " +
                        "`date_epoch_day` INTEGER NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL, " +
                        "`grams` REAL NOT NULL, " +
                        "`kcal` REAL NOT NULL, " +
                        "`protein_g` REAL NOT NULL, " +
                        "`carbs_g` REAL NOT NULL, " +
                        "`fat_g` REAL NOT NULL, " +
                        "`fiber_g` REAL NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_meal_log_date_epoch_day_status` " +
                        "ON `meal_log` (`date_epoch_day`, `status`)"
                )
            }
        }

        /**
         * v3 -> v4 (Phase 4): adds the exercise_catalog, gym_workout and exercise_set tables. Existing
         * data is untouched. The catalog is created empty and filled from assets/gym_seed.json by
         * GymCatalogSeeder. As with the earlier migrations, the DDL must match the Room-generated schema
         * exactly (column types, NOT NULL, index names, foreign key), otherwise Room refuses to open the DB.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `exercise_catalog` (" +
                        "`exercise_id` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`muscle_group` TEXT NOT NULL, " +
                        "`equipment_type` TEXT NOT NULL, " +
                        "`instructions` TEXT NOT NULL, " +
                        "PRIMARY KEY(`exercise_id`))"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_exercise_catalog_name` " +
                        "ON `exercise_catalog` (`name`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_exercise_catalog_muscle_group` " +
                        "ON `exercise_catalog` (`muscle_group`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `gym_workout` (" +
                        "`session_id` TEXT NOT NULL, " +
                        "`epoch_day` INTEGER NOT NULL, " +
                        "`start_time` INTEGER NOT NULL, " +
                        "`end_time` INTEGER, " +
                        "`notes` TEXT, " +
                        "PRIMARY KEY(`session_id`))"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_gym_workout_epoch_day` " +
                        "ON `gym_workout` (`epoch_day`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_gym_workout_start_time` " +
                        "ON `gym_workout` (`start_time`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `exercise_set` (" +
                        "`set_id` TEXT NOT NULL, " +
                        "`workout_id` TEXT NOT NULL, " +
                        "`exercise_id` TEXT NOT NULL, " +
                        "`set_index` INTEGER NOT NULL, " +
                        "`weight_kg` REAL NOT NULL, " +
                        "`reps` INTEGER NOT NULL, " +
                        "`rpe` REAL, " +
                        "`set_type` TEXT NOT NULL, " +
                        "PRIMARY KEY(`set_id`), " +
                        "FOREIGN KEY(`workout_id`) REFERENCES `gym_workout`(`session_id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_exercise_set_workout_id_exercise_id_set_index` " +
                        "ON `exercise_set` (`workout_id`, `exercise_id`, `set_index`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_exercise_set_exercise_id` " +
                        "ON `exercise_set` (`exercise_id`)"
                )
            }
        }
    }
}
