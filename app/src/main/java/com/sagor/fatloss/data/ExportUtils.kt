package com.sagor.fatloss.data

import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

class ExportUtils(private val repository: FatLossRepository) {
    suspend fun exportJson(): String {
        val profile = repository.profile.first()
        val weights = repository.weights.first()
        val sleeps = repository.sleepHistory.first()
        val foods = repository.allFoods.first()
        val steps = repository.allSteps.first()
        val workouts = repository.allWorkoutProgress.first()
        val exerciseChanges = repository.workoutExerciseChanges.first()
        return JSONObject()
            .put("profile", JSONObject()
                .put("name", profile.name)
                .put("age", profile.age)
                .put("heightCm", profile.heightCm)
                .put("startWeightKg", profile.startWeightKg)
                .put("currentWeightKg", profile.currentWeightKg)
                .put("targetWeightKg", profile.targetWeightKg)
                .put("calorieTarget", profile.calorieTarget)
                .put("proteinTarget", profile.proteinTarget)
                .put("stepGoal", profile.stepGoal)
                .put("startDate", profile.startDate))
            .put("weights", JSONArray(weights.map { JSONObject().put("date", it.date).put("weightKg", it.weightKg) }))
            .put("sleep", JSONArray(sleeps.map { JSONObject().put("date", it.date).put("bedtime", it.bedtime).put("wakeTime", it.wakeTime).put("hours", it.hours) }))
            .put("foods", JSONArray(foods.map { JSONObject().put("date", it.date).put("mealType", it.mealType).put("name", it.name).put("calories", it.calories).put("protein", it.protein) }))
            .put("steps", JSONArray(steps.map { JSONObject().put("date", it.date).put("steps", it.steps) }))
            .put("workouts", JSONArray(workouts.map { JSONObject().put("date", it.date).put("exerciseName", it.exerciseName).put("completed", it.completed) }))
            .put("exerciseChanges", JSONArray(exerciseChanges.map {
                JSONObject()
                    .put("id", it.id)
                    .put("day", it.day)
                    .put("sourceName", it.sourceName)
                    .put("name", it.name)
                    .put("prescription", it.prescription)
                    .put("restSeconds", it.restSeconds)
                    .put("instruction", it.instruction)
                    .put("orderIndex", it.orderIndex)
                    .put("deleted", it.deleted)
            }))
            .toString(2)
    }

    suspend fun exportCsv(): String {
        val weights = repository.weights.first()
        return buildString {
            appendLine("type,date,value")
            weights.forEach { appendLine("weight,${it.date},${it.weightKg}") }
            repository.sleepHistory.first().forEach { appendLine("sleep,${it.date},${it.hours}") }
            repository.allSteps.first().forEach { appendLine("steps,${it.date},${it.steps}") }
            repository.allWorkoutProgress.first().filter { it.completed }.forEach {
                appendLine("workout,${it.date},${csvCell(it.exerciseName)}")
            }
        }
    }

    suspend fun importJson(text: String) {
        val root = JSONObject(text)
        if (root.has("profile")) {
            val p = root.getJSONObject("profile")
            val current = p.optDouble("currentWeightKg", repository.profile.first().currentWeightKg)
            val target = p.optDouble("targetWeightKg", repository.profile.first().targetWeightKg)
            val name = p.optString("name", repository.profile.first().name)
            val old = repository.profile.first()
            repository.saveProfile(
                old.copy(
                    name = name,
                    age = p.optInt("age", old.age),
                    heightCm = p.optInt("heightCm", old.heightCm),
                    startWeightKg = p.optDouble("startWeightKg", old.startWeightKg),
                    currentWeightKg = current,
                    targetWeightKg = target,
                    calorieTarget = p.optInt("calorieTarget", old.calorieTarget),
                    proteinTarget = p.optInt("proteinTarget", old.proteinTarget),
                    stepGoal = p.optInt("stepGoal", old.stepGoal),
                    startDate = p.optString("startDate", old.startDate)
                )
            )
        }
        root.optJSONArray("weights")?.let { weights ->
            repeat(weights.length()) { index ->
                val item = weights.optJSONObject(index) ?: return@repeat
                val date = item.optString("date")
                if (date.isNotBlank()) {
                    repository.addWeight(date, item.optDouble("weightKg", repository.profile.first().currentWeightKg))
                }
            }
        }
        root.optJSONArray("steps")?.let { steps ->
            repeat(steps.length()) { index ->
                val item = steps.optJSONObject(index) ?: return@repeat
                val date = item.optString("date")
                if (date.isNotBlank()) {
                    repository.saveSteps(date, item.optInt("steps", 0))
                }
            }
        }
        root.optJSONArray("workouts")?.let { workouts ->
            repeat(workouts.length()) { index ->
                val item = workouts.optJSONObject(index) ?: return@repeat
                val date = item.optString("date")
                val exercise = item.optString("exerciseName")
                if (date.isNotBlank() && exercise.isNotBlank()) {
                    repository.setExercise(date, exercise, item.optBoolean("completed", false))
                }
            }
        }
        root.optJSONArray("exerciseChanges")?.let { changes ->
            repeat(changes.length()) { index ->
                val item = changes.optJSONObject(index) ?: return@repeat
                val id = item.optString("id")
                if (id.isNotBlank()) {
                    repository.saveWorkoutExercise(
                        WorkoutExerciseEntity(
                            id = id,
                            day = item.optInt("day", 1).coerceIn(1, 7),
                            sourceName = item.optString("sourceName").ifBlank { null },
                            name = item.optString("name", "Exercise"),
                            prescription = item.optString("prescription", "3 x 12"),
                            restSeconds = item.optInt("restSeconds", 60).coerceAtLeast(15),
                            instruction = item.optString("instruction", "Keep clean form."),
                            orderIndex = item.optInt("orderIndex", index),
                            deleted = item.optBoolean("deleted", false)
                        )
                    )
                }
            }
        }
    }

    private fun csvCell(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' }) "\"${value.replace("\"", "\"\"")}\"" else value
}
