// app/src/main/java/com/calovelo/app/di/DatabaseModule.kt
package com.calovelo.app.di

import android.content.Context
import androidx.room.Room
import com.calovelo.app.BuildConfig
import com.calovelo.app.data.local.CaloveloDatabase
import com.calovelo.app.data.local.dao.FoodCatalogDao
import com.calovelo.app.data.local.dao.HealthSummaryDao
import com.calovelo.app.data.local.dao.MealLogDao
import com.calovelo.app.data.local.dao.WaterLogDao
import com.calovelo.app.data.local.dao.WorkoutSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CaloveloDatabase =
        Room.databaseBuilder(context, CaloveloDatabase::class.java, CaloveloDatabase.DATABASE_NAME)
            .addMigrations(CaloveloDatabase.MIGRATION_1_2, CaloveloDatabase.MIGRATION_2_3)
            .apply {
                // Debug builds may wipe the DB if a migration is missing while the schema is in
                // flux. Release builds never do: they fail loudly instead of deleting user data.
                if (BuildConfig.DEBUG) fallbackToDestructiveMigration()
            }
            .build()

    @Provides
    fun provideHealthSummaryDao(db: CaloveloDatabase): HealthSummaryDao = db.healthSummaryDao()

    @Provides
    fun provideWaterLogDao(db: CaloveloDatabase): WaterLogDao = db.waterLogDao()

    @Provides
    fun provideWorkoutSessionDao(db: CaloveloDatabase): WorkoutSessionDao = db.workoutSessionDao()

    @Provides
    fun provideFoodCatalogDao(db: CaloveloDatabase): FoodCatalogDao = db.foodCatalogDao()

    @Provides
    fun provideMealLogDao(db: CaloveloDatabase): MealLogDao = db.mealLogDao()
}
