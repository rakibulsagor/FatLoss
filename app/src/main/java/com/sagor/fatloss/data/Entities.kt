package com.sagor.fatloss.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Rakibul Hasan Sagor",
    val age: Int = 22,
    val gender: String = "Male",
    val heightCm: Int = 175,
    val startWeightKg: Double = 80.0,
    val currentWeightKg: Double = 80.0,
    val targetWeightKg: Double = 65.0,
    val calorieTarget: Int = 1800,
    val proteinTarget: Int = 120,
    val stepGoal: Int = 8000,
    val startDate: String = today()
)

@Entity
data class DailyTask(
    @PrimaryKey val id: String,
    val date: String,
    val walkDone: Boolean = false,
    val workoutDone: Boolean = false,
    val dietDone: Boolean = false,
    val sleepDone: Boolean = false,
    val brokenSleepAvoided: Boolean = false,
    val dinnerRiceAvoided: Boolean = false,
    val latePhoneAvoided: Boolean = false
)

@Entity
data class WorkoutProgress(
    @PrimaryKey val id: String,
    val date: String,
    val exerciseName: String,
    val completed: Boolean
)

@Entity
data class WorkoutExerciseEntity(
    @PrimaryKey val id: String,
    val day: Int,
    val sourceName: String? = null,
    val name: String,
    val prescription: String,
    val restSeconds: Int,
    val instruction: String,
    val orderIndex: Int,
    val deleted: Boolean = false
)

@Entity
data class FoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val mealType: String,
    val name: String,
    val calories: Int,
    val protein: Int
)

@Entity
data class StepEntry(
    @PrimaryKey val date: String,
    val steps: Int,
    val sensorBaseline: Float? = null
)

@Entity
data class SleepEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val bedtime: String,
    val wakeTime: String,
    val hours: Double
)

@Entity
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val weightKg: Double
)

@Entity
data class PhotoEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val localPath: String
)
