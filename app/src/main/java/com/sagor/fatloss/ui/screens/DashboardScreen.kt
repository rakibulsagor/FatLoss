package com.sagor.fatloss.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sagor.fatloss.data.PlanData
import com.sagor.fatloss.ui.Blue
import com.sagor.fatloss.ui.CheckRow
import com.sagor.fatloss.ui.Coral
import com.sagor.fatloss.ui.Green
import com.sagor.fatloss.ui.HeroBanner
import com.sagor.fatloss.ui.MetricRing
import com.sagor.fatloss.ui.PlanCard
import com.sagor.fatloss.ui.ProgressLine
import com.sagor.fatloss.ui.Section
import com.sagor.fatloss.ui.TopBar
import com.sagor.fatloss.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(vm: DashboardViewModel, onProfileClick: () -> Unit = {}) {
    val state by vm.state.collectAsState()
    val workout = PlanData.todayWorkout()
    val weightProgress = ((state.profile.startWeightKg - state.profile.currentWeightKg) /
            (state.profile.startWeightKg - state.profile.targetWeightKg)).toFloat()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar("Fat Loss", onProfileClick = onProfileClick)
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            androidx.compose.material3.Text(
                "Salam, ${state.profile.name.substringBefore(' ').ifBlank { "Sagor" }}",
                color = com.sagor.fatloss.ui.Muted,
                fontFamily = FontFamily.Cursive,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
            androidx.compose.material3.Text(
                "You're on track for 65kg!",
                color = Green,
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 18.dp)
            )
            PlanCard("Current Weight", "", accent = Green) {
                Row(Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Text(
                        "%.1f".format(state.profile.currentWeightKg),
                        color = androidx.compose.ui.graphics.Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.weight(1f)
                    )
                    androidx.compose.material3.Text(
                        "TARGET\n%.0f kg".format(state.profile.targetWeightKg),
                        color = com.sagor.fatloss.ui.Muted,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
                ProgressLine(
                    "Start: %.0fkg".format(state.profile.startWeightKg),
                    weightProgress,
                    "%.0fkg to go".format((state.profile.currentWeightKg - state.profile.targetWeightKg).coerceAtLeast(0.0))
                )
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 18.dp)) {
                MetricRing("Cal", state.calories.toString(), "/${state.profile.calorieTarget} kcal", state.calories / state.profile.calorieTarget.toFloat(), Green, Modifier.weight(1f).padding(end = 6.dp))
                MetricRing("Pro", "${state.protein}g", "/${state.profile.proteinTarget}g", state.protein / state.profile.proteinTarget.toFloat(), Blue, Modifier.weight(1f).padding(horizontal = 3.dp))
                MetricRing("Steps", if (state.steps > 999) "${state.steps / 1000.0}k" else state.steps.toString(), "/${state.profile.stepGoal}", state.steps / state.profile.stepGoal.toFloat(), Coral, Modifier.weight(1f).padding(start = 6.dp))
            }
            HeroBanner("Never Miss Twice", "Rule of the day", Modifier.padding(bottom = 22.dp))
            Section("Today's Protocol")
            PlanCard("", "", accent = Green) {
                CheckRow("Morning Fasted Walk (30 min)", state.walkDone) { vm.toggle("walk", it) }
                CheckRow("Workout Done - ${workout.title}", state.workoutDone) { vm.toggle("workout", it) }
                CheckRow("Followed Diet Rules", state.dietDone) { vm.toggle("diet", it) }
                CheckRow("3.5L Water Completed", state.dietDone) { vm.toggle("diet", it) }
                CheckRow("Sleep/Phone Rules Followed", state.sleepDone) { vm.toggle("sleep", it) }
            }
            PlanCard("Next Workout", "${workout.title}\n${workout.walk}", accent = Blue)
        }
    }
}
