package com.sagor.fatloss.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM UserProfile WHERE id = 1")
    fun profile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfile)

    @Query("SELECT * FROM DailyTask WHERE date = :date LIMIT 1")
    fun task(date: String): Flow<DailyTask?>

    @Query("SELECT * FROM DailyTask ORDER BY date DESC")
    fun tasks(): Flow<List<DailyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTask(task: DailyTask)

    @Query("SELECT * FROM FoodEntry WHERE date = :date ORDER BY id DESC")
    fun foods(date: String): Flow<List<FoodEntry>>

    @Query("SELECT * FROM FoodEntry ORDER BY date DESC, id DESC")
    fun allFoods(): Flow<List<FoodEntry>>

    @Insert
    suspend fun addFood(entry: FoodEntry)

    @Query("DELETE FROM FoodEntry WHERE id = :id")
    suspend fun deleteFood(id: Long)

    @Query("SELECT * FROM StepEntry WHERE date = :date LIMIT 1")
    fun steps(date: String): Flow<StepEntry?>

    @Query("SELECT * FROM StepEntry ORDER BY date DESC")
    fun allSteps(): Flow<List<StepEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSteps(entry: StepEntry)

    @Query("SELECT * FROM SleepEntry ORDER BY date DESC, id DESC LIMIT 30")
    fun sleepHistory(): Flow<List<SleepEntry>>

    @Insert
    suspend fun addSleep(entry: SleepEntry)

    @Query("SELECT * FROM WeightEntry ORDER BY date DESC, id DESC")
    fun weights(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM WeightEntry WHERE date = :date LIMIT 1")
    suspend fun weightForDate(date: String): WeightEntry?

    @Query("UPDATE WeightEntry SET weightKg = :weight WHERE date = :date")
    suspend fun updateWeightForDate(date: String, weight: Double)

    @Insert
    suspend fun addWeight(entry: WeightEntry)

    @Query("SELECT * FROM WorkoutProgress WHERE date = :date")
    fun workoutProgress(date: String): Flow<List<WorkoutProgress>>

    @Query("SELECT * FROM WorkoutProgress ORDER BY date DESC")
    fun allWorkoutProgress(): Flow<List<WorkoutProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWorkoutProgress(progress: WorkoutProgress)

    @Query("SELECT * FROM WorkoutExerciseEntity ORDER BY day ASC, orderIndex ASC")
    fun workoutExerciseChanges(): Flow<List<WorkoutExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWorkoutExercise(exercise: WorkoutExerciseEntity)

    @Query("DELETE FROM WorkoutExerciseEntity WHERE id = :id")
    suspend fun deleteWorkoutExercise(id: String)

    @Query("SELECT * FROM PhotoEntry ORDER BY date DESC, id DESC")
    fun photos(): Flow<List<PhotoEntry>>

    @Insert
    suspend fun addPhoto(entry: PhotoEntry)
}
