package com.sagor.fatloss.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import com.sagor.fatloss.data.PlanData
import com.sagor.fatloss.health.HealthConnectSnapshot
import com.sagor.fatloss.ui.Blue
import com.sagor.fatloss.ui.Bg
import com.sagor.fatloss.ui.Coral
import com.sagor.fatloss.ui.Green
import com.sagor.fatloss.ui.HeroBanner
import com.sagor.fatloss.ui.MetricRing
import com.sagor.fatloss.ui.Muted
import com.sagor.fatloss.ui.PlanCard
import com.sagor.fatloss.ui.ProgressLine
import com.sagor.fatloss.ui.Section
import com.sagor.fatloss.ui.StatCard
import com.sagor.fatloss.ui.Surface2
import com.sagor.fatloss.ui.TinyChart
import com.sagor.fatloss.ui.TopBar
import com.sagor.fatloss.viewmodel.AnalyticsViewModel
import com.sagor.fatloss.wellbeing.WellbeingSnapshot
import kotlinx.coroutines.delay

@Composable
fun AnalyticsScreen(vm: AnalyticsViewModel, onProfileClick: () -> Unit = {}) {
    val state by vm.state.collectAsState()
    val health by vm.health.collectAsState()
    val wellbeing by vm.wellbeing.collectAsState()
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    val healthPermissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) {
        vm.refreshHealthConnect()
    }
    LaunchedEffect(Unit) {
        delay(120)
        visible = true
    }

    val weightProgress = ((state.profile.startWeightKg - state.currentWeight) /
            (state.profile.startWeightKg - state.profile.targetWeightKg)).toFloat()
    val phase = PlanData.currentPhase(state.profile.startDate)
    val completionRate = if (state.trackedDays == 0) 0f else state.completedDays / state.trackedDays.toFloat()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar("Fat Loss", "AN", onProfileClick = onProfileClick)
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 5 })
        ) {
            Column(Modifier.padding(16.dp)) {
                Section("Analytics")
                Text(
                    "All your fat-loss signals in one command center.",
                    color = Muted,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row {
                    MetricRing(
                        "Weight",
                        "%.1f".format(state.currentWeight),
                        "kg now",
                        weightProgress,
                        Green,
                        Modifier.weight(1f).padding(end = 6.dp)
                    )
                    MetricRing(
                        "Score",
                        "${state.todayScore}/4",
                        "today",
                        state.todayScore / 4f,
                        Blue,
                        Modifier.weight(1f).padding(horizontal = 3.dp)
                    )
                    MetricRing(
                        "Sleep",
                        "%.1f".format(state.avgSleep),
                        "avg hrs",
                        (state.avgSleep / 8.0).toFloat(),
                        Coral,
                        Modifier.weight(1f).padding(start = 6.dp)
                    )
                }

                PlanCard("Weight Analytics", "Start: %.1f kg\nCurrent: %.1f kg\nLost: %.1f kg\nTarget: %.1f kg".format(
                    state.profile.startWeightKg,
                    state.currentWeight,
                    state.weightLost,
                    state.profile.targetWeightKg
                ), accent = Green) {
                    ProgressLine("Progress to 65kg", weightProgress, "%.0f%%".format(weightProgress.coerceIn(0f, 1f) * 100))
                    TinyChart(state.weightSeries.ifEmpty { listOf(state.profile.startWeightKg, state.currentWeight) }, state.profile.targetWeightKg)
                }

                Row {
                    StatCard("BMI", "%.1f".format(state.bmi), "current", Modifier.weight(1f).padding(end = 4.dp))
                    StatCard("Target BMI", "%.1f".format(state.targetBmi), "normal", Modifier.weight(1f).padding(start = 4.dp))
                }

                Section("Nutrition")
                PlanCard("Calorie and protein trend", "Average calories: %.0f / ${state.profile.calorieTarget} kcal\nAverage protein: %.0f / ${state.profile.proteinTarget}g".format(
                    state.avgCalories,
                    state.avgProtein
                ), accent = Blue) {
                    ProgressLine("Calories", (state.avgCalories / state.profile.calorieTarget).toFloat(), "%.0f kcal avg".format(state.avgCalories))
                    ProgressLine("Protein", (state.avgProtein / state.profile.proteinTarget).toFloat(), "%.0fg avg".format(state.avgProtein))
                }

                Section("Movement")
                PlanCard("Steps and walking", "Average steps: %.0f\nBest day: ${state.bestSteps}\nGoal: ${state.profile.stepGoal}".format(state.avgSteps), accent = Green) {
                    ProgressLine("Average step goal", (state.avgSteps / state.profile.stepGoal).toFloat(), "%.0f / ${state.profile.stepGoal}".format(state.avgSteps))
                }

                DeviceHealthSection(
                    health = health,
                    wellbeing = wellbeing,
                    onRequestHealth = { healthPermissionLauncher.launch(vm.healthPermissions) },
                    onSyncHealth = { vm.refreshHealthConnect() },
                    onOpenHealthConnect = { context.startActivity(vm.openHealthConnectProvider()) },
                    onOpenUsageAccess = { context.startActivity(vm.openUsageAccess()) },
                    onSyncWellbeing = { vm.refreshWellbeing() }
                )

                Section("Consistency")
                PlanCard("Never miss twice analytics", "Completed days: ${state.completedDays}\nTracked days: ${state.trackedDays}\nProgress photos: ${state.photoCount}", accent = Coral) {
                    ProgressLine("Completion rate", completionRate, "%.0f%%".format(completionRate * 100))
                }

                HeroBanner("Phase $phase Active", PlanData.phases.first { it.number == phase }.title)

                Section("Coach Notes")
                PlanCard(
                    "What the numbers mean",
                    buildString {
                        appendLine(if (state.avgSleep < 7.0) "Sleep is the weak point. Protect the 10:30 PM phone rule." else "Sleep trend is useful. Keep it steady.")
                        appendLine(if (state.avgProtein < state.profile.proteinTarget * .8) "Protein is low. Add eggs, dal, mach, or murgi." else "Protein is strong enough to protect muscle.")
                        appendLine(if (state.avgSteps < state.profile.stepGoal * .75) "Steps are below target. Morning walk is the anchor." else "Walking volume is supporting the cut.")
                    }.trim(),
                    accent = Blue
                )
            }
        }
    }
}

@Composable
private fun DeviceHealthSection(
    health: HealthConnectSnapshot,
    wellbeing: WellbeingSnapshot,
    onRequestHealth: () -> Unit,
    onSyncHealth: () -> Unit,
    onOpenHealthConnect: () -> Unit,
    onOpenUsageAccess: () -> Unit,
    onSyncWellbeing: () -> Unit
) {
    Section("Device Health")
    PlanCard("Health Connect", health.message, accent = Green) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Steps", health.steps.toString(), "today", Modifier.weight(1f))
            StatCard("Distance", "%.2f".format(health.distanceKm), "km", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            StatCard("Active", "%.0f".format(health.activeCalories), "kcal", Modifier.weight(1f))
            StatCard("Sleep", "%.1f".format(health.sleepHours), "hours", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            StatCard("Heart", if (health.averageHeartRate > 0) "%.0f".format(health.averageHeartRate) else "--", "bpm avg", Modifier.weight(1f))
            StatCard("Weight", if (health.weightKg > 0) "%.1f".format(health.weightKg) else "--", "kg", Modifier.weight(1f))
        }
        if (health.lastSync.isNotBlank()) {
            Text("Last sync: ${health.lastSync}", color = Muted, modifier = Modifier.padding(top = 10.dp))
        }
        ActionButton(
            text = if (health.permissionGranted) "Sync Health Connect" else "Grant Health Connect Access",
            onClick = if (health.permissionGranted) onSyncHealth else onRequestHealth
        )
        ActionButton("Open / Update Health Connect", onOpenHealthConnect)
    }

    PlanCard("Wellbeing Data", wellbeing.message, accent = Blue) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Screen", formatMinutes(wellbeing.screenMinutes), "today", Modifier.weight(1f))
            StatCard("Unlocks", wellbeing.unlocks.toString(), "today", Modifier.weight(1f))
        }
        StatCard("Apps Used", wellbeing.appCount.toString(), "foreground apps", Modifier.fillMaxWidth().padding(top = 8.dp))
        wellbeing.topApps.forEach { app ->
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(app.label.take(22), color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                Text(formatMinutes(app.minutes), color = Muted)
            }
        }
        ActionButton(
            text = if (wellbeing.permissionGranted) "Refresh Wellbeing" else "Grant Usage Access",
            onClick = if (wellbeing.permissionGranted) onSyncWellbeing else onOpenUsageAccess
        )
    }
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Surface2, contentColor = Green),
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
    ) {
        Text(text.uppercase(), fontWeight = FontWeight.Black)
    }
}

private fun formatMinutes(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}
