package com.sagor.fatloss.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class ExercisePlan(val name: String, val prescription: String, val restSeconds: Int, val instruction: String)
data class WorkoutPlan(val day: DayOfWeek, val title: String, val type: String, val walk: String, val exercises: List<ExercisePlan>)
data class FoodSuggestion(val name: String, val calories: Int, val protein: Int)
data class PhasePlan(val number: Int, val weeks: String, val title: String, val goals: List<String>, val expected: String)

object PlanData {
    val workouts = listOf(
        WorkoutPlan(DayOfWeek.MONDAY, "Upper Strength", "Strength", "30 min fasted walk", listOf(
            ExercisePlan("Push-ups", "4 x 12", 60, "Keep body straight, chest near floor."),
            ExercisePlan("Wide push-ups", "3 x 12", 60, "Hands wider than shoulders."),
            ExercisePlan("Diamond push-ups", "3 x 8", 60, "Hands close, elbows controlled."),
            ExercisePlan("Pike push-ups", "3 x 10", 60, "Hips high, press through shoulders."),
            ExercisePlan("Plank", "3 x 45 sec", 45, "Brace belly, do not sag."),
            ExercisePlan("Superman hold", "3 x 30 sec", 45, "Squeeze glutes and upper back.")
        )),
        WorkoutPlan(DayOfWeek.TUESDAY, "HIIT", "HIIT", "30 min fasted walk", hiitExercises()),
        WorkoutPlan(DayOfWeek.WEDNESDAY, "Active Recovery", "Recovery", "45 min fasted walk", emptyList()),
        WorkoutPlan(DayOfWeek.THURSDAY, "Lower Strength", "Strength", "30 min fasted walk", listOf(
            ExercisePlan("Bodyweight squats", "4 x 20", 60, "Sit hips back, knees track toes."),
            ExercisePlan("Bulgarian split squat", "3 x 12 each leg", 60, "Use bed or chair if available."),
            ExercisePlan("Glute bridge", "4 x 20", 45, "Pause at top, squeeze glutes."),
            ExercisePlan("Reverse lunge", "3 x 12 each leg", 60, "Step back softly, torso tall."),
            ExercisePlan("Standing calf raise", "4 x 25", 30, "Full range, slow lower."),
            ExercisePlan("Wall sit", "3 x 45 sec", 60, "Thighs near parallel.")
        )),
        WorkoutPlan(DayOfWeek.FRIDAY, "Full Body", "Strength", "30 min fasted walk", listOf(
            ExercisePlan("Jump squat", "4 x 15", 60, "Land quietly and reset."),
            ExercisePlan("Push-ups", "3 x 15", 45, "Clean reps over speed."),
            ExercisePlan("Glute bridge", "3 x 20", 45, "Pause at top."),
            ExercisePlan("Mountain climbers", "3 x 30 sec", 45, "Fast feet, stable shoulders."),
            ExercisePlan("Plank to downward dog", "3 x 10", 45, "Move smoothly."),
            ExercisePlan("Burpees", "3 x 10", 60, "Step back if jumping is too hard.")
        )),
        WorkoutPlan(DayOfWeek.SATURDAY, "HIIT", "HIIT", "30 min fasted walk", hiitExercises()),
        WorkoutPlan(DayOfWeek.SUNDAY, "Rest", "Rest", "Optional 30 min easy walk", emptyList())
    )

    val foodSuggestions = listOf(
        FoodSuggestion("Ruti - 1 piece", 150, 5),
        FoodSuggestion("Rice - 1 cup cooked", 200, 4),
        FoodSuggestion("Dal - 1 bowl", 180, 12),
        FoodSuggestion("Egg - 1 whole", 70, 6),
        FoodSuggestion("Mach - family portion", 220, 28),
        FoodSuggestion("Murgi - family portion", 240, 32),
        FoodSuggestion("Mangsho - small portion", 300, 30),
        FoodSuggestion("Shobji - full portion", 120, 4)
    )

    val phases = listOf(
        PhasePlan(1, "Weeks 1-4", "Foundation", listOf(
            "Fix sleep to 1:00 AM from 2-3 AM",
            "Cut dinner rice completely from day 1",
            "Walk 30 min every morning fasted",
            "Workout 4x per week",
            "Log food daily"
        ), "Expected loss: 2-3 kg"),
        PhasePlan(2, "Weeks 5-12", "Momentum", listOf(
            "Sleep fixed to 11:30 PM or 12:00 AM",
            "Walk increased to 40-45 min",
            "Increase HIIT intensity",
            "Lunch rice reduced to 0.75 cup",
            "8,000+ steps daily"
        ), "Expected total loss: 6-8 kg"),
        PhasePlan(3, "Weeks 13-24", "Finish", listOf(
            "Sleep at 11:00 PM consistently",
            "Walk 45-60 min",
            "Add Sunday easy walk",
            "Lunch rice at 0.5 cup or swap to ruti",
            "10,000 steps daily target"
        ), "Expected total loss: 13-15 kg")
    )

    fun todayWorkout(): WorkoutPlan = workouts.first { it.day == LocalDate.now().dayOfWeek }
    fun nextWorkout(): WorkoutPlan = todayWorkout().takeUnless { it.exercises.isEmpty() } ?: workouts.first { it.exercises.isNotEmpty() }

    fun currentPhase(startDate: String): Int {
        val weeks = runCatching { ChronoUnit.WEEKS.between(LocalDate.parse(startDate), LocalDate.now()).toInt() + 1 }.getOrDefault(1)
        return when {
            weeks <= 4 -> 1
            weeks <= 12 -> 2
            else -> 3
        }
    }

    private fun hiitExercises() = listOf(
        ExercisePlan("Burpees", "40 sec work / 20 sec rest x 4", 20, "Move at a pace you can repeat."),
        ExercisePlan("High knees", "40 sec work / 20 sec rest x 4", 20, "Drive knees up, keep arms active."),
        ExercisePlan("Jump squats", "40 sec work / 20 sec rest x 4", 20, "Land soft."),
        ExercisePlan("Mountain climbers", "40 sec work / 20 sec rest x 4", 20, "Core tight, shoulders stacked.")
    )
}
