package com.sagor.fatloss.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserProfile::class,
        DailyTask::class,
        WorkoutProgress::class,
        WorkoutExerciseEntity::class,
        FoodEntry::class,
        StepEntry::class,
        SleepEntry::class,
        WeightEntry::class,
        PhotoEntry::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sagor-fat-loss.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `WorkoutExerciseEntity` (
                        `id` TEXT NOT NULL,
                        `day` INTEGER NOT NULL,
                        `sourceName` TEXT,
                        `name` TEXT NOT NULL,
                        `prescription` TEXT NOT NULL,
                        `restSeconds` INTEGER NOT NULL,
                        `instruction` TEXT NOT NULL,
                        `orderIndex` INTEGER NOT NULL,
                        `deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `UserProfile` ADD COLUMN `gender` TEXT NOT NULL DEFAULT 'Male'")
            }
        }
    }
}
