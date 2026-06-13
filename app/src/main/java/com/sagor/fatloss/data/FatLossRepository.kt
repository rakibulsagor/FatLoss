package com.sagor.fatloss.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FatLossRepository(private val dao: AppDao) {
    val profile: Flow<UserProfile> = dao.profile().map { it ?: UserProfile() }
    val weights = dao.weights()
    val sleepHistory = dao.sleepHistory()
    val photos = dao.photos()
    val tasks = dao.tasks()
    val allFoods = dao.allFoods()
    val allSteps = dao.allSteps()
    val allWorkoutProgress = dao.allWorkoutProgress()
    val workoutExerciseChanges = dao.workoutExerciseChanges()

    fun task(date: String) = dao.task(date).map { it ?: DailyTask(id = date, date = date) }
    fun foods(date: String) = dao.foods(date)
    fun steps(date: String) = dao.steps(date).map { it ?: StepEntry(date, 0) }
    fun workoutProgress(date: String) = dao.workoutProgress(date)

    suspend fun saveProfile(profile: UserProfile) = dao.saveProfile(profile)

    suspend fun updateTask(date: String, transform: (DailyTask) -> DailyTask) {
        val current = task(date).first()
        dao.saveTask(transform(current))
    }

    suspend fun markAppUsed(date: String) {
        val current = task(date).first()
        dao.saveTask(current)
    }

    suspend fun addFood(date: String, mealType: String, name: String, calories: Int, protein: Int) {
        dao.addFood(FoodEntry(date = date, mealType = mealType, name = name, calories = calories, protein = protein))
    }

    suspend fun deleteFood(id: Long) = dao.deleteFood(id)

    suspend fun saveSteps(date: String, steps: Int, baseline: Float? = null) =
        dao.saveSteps(StepEntry(date, steps, baseline))

    suspend fun addSleep(date: String, bedtime: String, wake: String) =
        dao.addSleep(SleepEntry(date = date, bedtime = bedtime, wakeTime = wake, hours = sleepHours(bedtime, wake)))

    suspend fun addWeight(date: String, weight: Double) {
        if (dao.weightForDate(date) == null) {
            dao.addWeight(WeightEntry(date = date, weightKg = weight))
        } else {
            dao.updateWeightForDate(date, weight)
        }
        val profile = profile.first()
        dao.saveProfile(profile.copy(currentWeightKg = weight))
    }

    suspend fun setExercise(date: String, name: String, done: Boolean) =
        dao.saveWorkoutProgress(WorkoutProgress("$date-$name", date, name, done))

    suspend fun saveWorkoutExercise(exercise: WorkoutExerciseEntity) =
        dao.saveWorkoutExercise(exercise)

    suspend fun deleteWorkoutExercise(id: String) = dao.deleteWorkoutExercise(id)

    suspend fun resetWorkoutDay(date: String, exerciseKeys: List<String>) {
        exerciseKeys.forEach { key ->
            dao.saveWorkoutProgress(WorkoutProgress("$date-$key", date, key, false))
        }
    }

    suspend fun addPhoto(date: String, localPath: String) =
        dao.addPhoto(PhotoEntry(date = date, localPath = localPath))
}
