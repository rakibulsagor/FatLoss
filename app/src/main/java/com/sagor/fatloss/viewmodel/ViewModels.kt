package com.sagor.fatloss.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sagor.fatloss.AppGraph
import com.sagor.fatloss.data.ExportUtils
import com.sagor.fatloss.data.FatLossRepository
import com.sagor.fatloss.data.FoodEntry
import com.sagor.fatloss.data.PhotoEntry
import com.sagor.fatloss.data.PlanData
import com.sagor.fatloss.data.SleepEntry
import com.sagor.fatloss.data.StepEntry
import com.sagor.fatloss.data.UserProfile
import com.sagor.fatloss.data.WeightEntry
import com.sagor.fatloss.data.DailyTask
import com.sagor.fatloss.data.WorkoutProgress
import com.sagor.fatloss.data.WorkoutExerciseEntity
import com.sagor.fatloss.data.sleepHours
import com.sagor.fatloss.data.today
import com.sagor.fatloss.health.HealthConnectSnapshot
import com.sagor.fatloss.wellbeing.WellbeingSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

open class BaseVm(protected val repo: FatLossRepository) : ViewModel()

class DashboardViewModel(repo: FatLossRepository) : BaseVm(repo) {
    val state = combine(repo.profile, repo.task(today()), repo.steps(today()), repo.foods(today()), repo.sleepHistory) { p, t, s, f, sleep ->
        DashboardState(p, t.walkDone, t.workoutDone, t.dietDone, t.sleepDone, s.steps, f.sumOf { it.calories }, f.sumOf { it.protein }, sleep.firstOrNull()?.hours ?: 0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())
    fun toggle(name: String, done: Boolean) = viewModelScope.launch {
        repo.updateTask(today()) {
            when (name) {
                "walk" -> it.copy(walkDone = done)
                "workout" -> it.copy(workoutDone = done)
                "diet" -> it.copy(dietDone = done)
                else -> it.copy(sleepDone = done)
            }
        }
    }
}

data class DashboardState(
    val profile: UserProfile = UserProfile(),
    val walkDone: Boolean = false,
    val workoutDone: Boolean = false,
    val dietDone: Boolean = false,
    val sleepDone: Boolean = false,
    val steps: Int = 0,
    val calories: Int = 0,
    val protein: Int = 0,
    val sleepHours: Double = 0.0
)

class WorkoutViewModel(repo: FatLossRepository) : BaseVm(repo) {
    val progress = repo.workoutProgress(today()).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val exerciseChanges = repo.workoutExerciseChanges.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun setExercise(key: String, done: Boolean) = viewModelScope.launch { repo.setExercise(today(), key, done) }
    fun saveExercise(exercise: WorkoutExerciseEntity) = viewModelScope.launch { repo.saveWorkoutExercise(exercise) }
    fun deleteExercise(exercise: WorkoutExerciseEntity) = viewModelScope.launch {
        if (exercise.sourceName == null) repo.deleteWorkoutExercise(exercise.id) else repo.saveWorkoutExercise(exercise.copy(deleted = true))
    }
    fun resetDay(keys: List<String>) = viewModelScope.launch { repo.resetWorkoutDay(today(), keys) }
}

class FoodViewModel(repo: FatLossRepository) : BaseVm(repo) {
    val state = combine(repo.profile, repo.foods(today())) { p, f -> FoodState(p, f) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FoodState())
    fun add(meal: String, name: String, calories: String, protein: String) = viewModelScope.launch {
        val c = calories.toIntOrNull()?.coerceAtLeast(0) ?: return@launch
        val p = protein.toIntOrNull()?.coerceAtLeast(0) ?: return@launch
        if (name.isNotBlank()) repo.addFood(today(), meal, name.trim(), c, p)
    }
    fun delete(id: Long) = viewModelScope.launch { repo.deleteFood(id) }
}

data class FoodState(val profile: UserProfile = UserProfile(), val foods: List<FoodEntry> = emptyList())

class StepsViewModel(private val graph: AppGraph) : BaseVm(graph.repository) {
    val state = combine(repo.profile, repo.steps(today()), graph.stepCounter.liveSteps) { p, saved, live ->
        StepsState(saved.steps.coerceAtLeast(live), p.stepGoal, graph.stepCounter.isAvailable)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StepsState())
    fun start() = graph.stepCounter.start()
    fun stop() = graph.stepCounter.stop()
}

data class StepsState(val steps: Int = 0, val goal: Int = 8000, val sensorAvailable: Boolean = false)

class SleepViewModel(repo: FatLossRepository) : BaseVm(repo) {
    val history = repo.sleepHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun add(bed: String, wake: String) = viewModelScope.launch {
        runCatching { sleepHours(bed, wake) }.onSuccess { repo.addSleep(today(), bed.uppercase(Locale.US), wake.uppercase(Locale.US)) }
    }
}

class ProgressViewModel(repo: FatLossRepository) : BaseVm(repo) {
    private val partial = combine(repo.profile, repo.weights, repo.photos, repo.tasks, repo.allSteps) { p, w, photos, tasks, steps ->
        ProgressPartial(p, w, photos, tasks, steps)
    }
    val state = combine(partial, repo.allWorkoutProgress) { partial, workouts ->
        val activeDates = activeDateSet(partial.tasks, workouts)
        val currentMonth = YearMonth.now()
        val activeDaysThisMonth = activeDates.count { runCatching { YearMonth.from(LocalDate.parse(it)) == currentMonth }.getOrDefault(false) }
        val totalSessions = activeDates.size
        val currentStreak = streakFrom(activeDates)
        val kmWalked = partial.steps.sumOf { it.steps } * 0.000762
        val cardioMinutes = totalSessions * 35
        val monthlyWorkoutCounts = (5 downTo 0).map { offset ->
            val month = currentMonth.minusMonths(offset.toLong())
            month to activeDates.count { date -> runCatching { YearMonth.from(LocalDate.parse(date)) == month }.getOrDefault(false) }
        }
        ProgressState(
            profile = partial.profile,
            weights = partial.weights,
            photoCount = partial.photos.size,
            photos = partial.photos,
            currentStreak = currentStreak,
            activeDaysThisMonth = activeDaysThisMonth,
            totalSessions = totalSessions,
            kmWalked = kmWalked,
            cardioMinutes = cardioMinutes,
            goalPerWeek = 4,
            activeDates = activeDates,
            monthlyWorkoutCounts = monthlyWorkoutCounts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressState())
    fun addWeight(value: String) = viewModelScope.launch {
        value.toDoubleOrNull()?.let { repo.addWeight(today(), it) }
    }
    fun addPhoto(path: String) = viewModelScope.launch { repo.addPhoto(today(), path) }
}

private data class ProgressPartial(
    val profile: UserProfile,
    val weights: List<WeightEntry>,
    val photos: List<PhotoEntry>,
    val tasks: List<DailyTask>,
    val steps: List<StepEntry>
)

data class ProgressState(
    val profile: UserProfile = UserProfile(),
    val weights: List<WeightEntry> = emptyList(),
    val photoCount: Int = 0,
    val photos: List<PhotoEntry> = emptyList(),
    val currentStreak: Int = 0,
    val activeDaysThisMonth: Int = 0,
    val totalSessions: Int = 0,
    val kmWalked: Double = 0.0,
    val cardioMinutes: Int = 0,
    val goalPerWeek: Int = 4,
    val activeDates: Set<String> = emptySet(),
    val monthlyWorkoutCounts: List<Pair<YearMonth, Int>> = emptyList()
)

private fun activeDateSet(tasks: List<DailyTask>, workouts: List<WorkoutProgress>): Set<String> {
    val taskDates = tasks.map { it.date }
    val workoutDates = workouts.filter { it.completed }.map { it.date }
    return (taskDates + workoutDates).toSet()
}

private fun streakFrom(activeDates: Set<String>): Int {
    var date = LocalDate.now()
    var streak = 0
    while (activeDates.contains(date.toString())) {
        streak++
        date = date.minusDays(1)
    }
    return streak
}

class SettingsViewModel(private val graph: AppGraph) : BaseVm(graph.repository) {
    val profile = repo.profile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())
    var exportText = kotlinx.coroutines.flow.MutableStateFlow("")
    fun save(profile: UserProfile) = viewModelScope.launch { repo.saveProfile(profile) }
    fun testReminder() = graph.reminders.showTestReminder("Fat Loss", "Phone away at 10:30 PM. Never miss twice.")
    fun exportJson() = viewModelScope.launch { exportText.value = ExportUtils(repo).exportJson() }
    fun exportCsv() = viewModelScope.launch { exportText.value = ExportUtils(repo).exportCsv() }
    fun importJson(text: String) = viewModelScope.launch {
        runCatching { ExportUtils(repo).importJson(text) }
        exportText.value = "Import complete"
    }
}

class MotivationViewModel(repo: FatLossRepository) : BaseVm(repo) {
    val task = repo.task(today()).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.sagor.fatloss.data.DailyTask(today(), today()))
    fun toggle(key: String, done: Boolean) = viewModelScope.launch {
        repo.updateTask(today()) {
            when (key) {
                "sleep" -> it.copy(brokenSleepAvoided = done)
                "rice" -> it.copy(dinnerRiceAvoided = done)
                else -> it.copy(latePhoneAvoided = done)
            }
        }
    }
}

class RoadmapViewModel(repo: FatLossRepository) : BaseVm(repo) {
    val profile = repo.profile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())
}

class AnalyticsViewModel(private val graph: AppGraph) : BaseVm(graph.repository) {
    val health = MutableStateFlow(HealthConnectSnapshot())
    val wellbeing = MutableStateFlow(WellbeingSnapshot())
    val healthPermissions = graph.healthConnect.readPermissions

    private val partial = combine(
        repo.profile,
        repo.weights,
        repo.sleepHistory,
        repo.allSteps,
        repo.allFoods
    ) { profile, weights, sleeps, steps, foods ->
        AnalyticsPartial(profile, weights, sleeps, steps, foods)
    }

    val state = combine(partial, repo.tasks, repo.photos) { partial, tasks, photos ->
        val groupedFoods = partial.foods.groupBy { it.date }
        val avgCalories = groupedFoods.values.map { entries -> entries.sumOf { it.calories } }.averageIntOrZero()
        val avgProtein = groupedFoods.values.map { entries -> entries.sumOf { it.protein } }.averageIntOrZero()
        val avgSteps = partial.steps.map { it.steps }.averageIntOrZero()
        val bestSteps = partial.steps.maxOfOrNull { it.steps } ?: 0
        val avgSleep = partial.sleeps.map { it.hours }.averageDoubleOrZero()
        val completedDays = tasks.count { it.walkDone && it.workoutDone && it.dietDone && it.sleepDone }
        val todayTask = tasks.firstOrNull { it.date == today() }
        val todayScore = listOf(
            todayTask?.walkDone == true,
            todayTask?.workoutDone == true,
            todayTask?.dietDone == true,
            todayTask?.sleepDone == true
        ).count { it }
        val currentWeight = partial.weights.firstOrNull()?.weightKg ?: partial.profile.currentWeightKg
        val weightLost = (partial.profile.startWeightKg - currentWeight).coerceAtLeast(0.0)
        val bmi = currentWeight / ((partial.profile.heightCm / 100.0) * (partial.profile.heightCm / 100.0))
        val targetBmi = partial.profile.targetWeightKg / ((partial.profile.heightCm / 100.0) * (partial.profile.heightCm / 100.0))
        AnalyticsState(
            profile = partial.profile,
            currentWeight = currentWeight,
            weightLost = weightLost,
            bmi = bmi,
            targetBmi = targetBmi,
            avgCalories = avgCalories,
            avgProtein = avgProtein,
            avgSteps = avgSteps,
            bestSteps = bestSteps,
            avgSleep = avgSleep,
            completedDays = completedDays,
            trackedDays = tasks.size,
            todayScore = todayScore,
            photoCount = photos.size,
            weightSeries = partial.weights.sortedBy { it.date }.map { it.weightKg }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsState())

    fun refreshHealthConnect() = viewModelScope.launch {
        val snapshot = runCatching { graph.healthConnect.readToday() }
            .getOrElse { HealthConnectSnapshot(message = it.message ?: "Health Connect sync failed.") }
        health.value = snapshot
        if (snapshot.permissionGranted && snapshot.steps > 0) {
            repo.saveSteps(today(), snapshot.steps.toInt())
        }
    }

    fun openHealthConnectProvider() = graph.healthConnect.providerInstallIntent()

    fun refreshWellbeing() = viewModelScope.launch {
        wellbeing.value = runCatching { graph.wellbeing.readToday() }
            .getOrElse { WellbeingSnapshot(message = it.message ?: "Wellbeing sync failed.") }
    }

    fun openUsageAccess() = graph.wellbeing.usageAccessIntent()
}

private data class AnalyticsPartial(
    val profile: UserProfile,
    val weights: List<WeightEntry>,
    val sleeps: List<SleepEntry>,
    val steps: List<StepEntry>,
    val foods: List<FoodEntry>
)

data class AnalyticsState(
    val profile: UserProfile = UserProfile(),
    val currentWeight: Double = 80.0,
    val weightLost: Double = 0.0,
    val bmi: Double = 26.1,
    val targetBmi: Double = 21.2,
    val avgCalories: Double = 0.0,
    val avgProtein: Double = 0.0,
    val avgSteps: Double = 0.0,
    val bestSteps: Int = 0,
    val avgSleep: Double = 0.0,
    val completedDays: Int = 0,
    val trackedDays: Int = 0,
    val todayScore: Int = 0,
    val photoCount: Int = 0,
    val weightSeries: List<Double> = emptyList()
)

private fun List<Int>.averageIntOrZero(): Double = if (isEmpty()) 0.0 else average()
private fun List<Double>.averageDoubleOrZero(): Double = if (isEmpty()) 0.0 else average()

class VmFactory(private val graph: AppGraph) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = graph.repository
        return when (modelClass) {
            DashboardViewModel::class.java -> DashboardViewModel(repo)
            WorkoutViewModel::class.java -> WorkoutViewModel(repo)
            FoodViewModel::class.java -> FoodViewModel(repo)
            StepsViewModel::class.java -> StepsViewModel(graph)
            SleepViewModel::class.java -> SleepViewModel(repo)
            ProgressViewModel::class.java -> ProgressViewModel(repo)
            RoadmapViewModel::class.java -> RoadmapViewModel(repo)
            AnalyticsViewModel::class.java -> AnalyticsViewModel(graph)
            SettingsViewModel::class.java -> SettingsViewModel(graph)
            MotivationViewModel::class.java -> MotivationViewModel(repo)
            else -> error("Unknown ViewModel ${modelClass.simpleName}")
        } as T
    }
}
