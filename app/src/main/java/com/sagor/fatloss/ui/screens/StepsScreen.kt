package com.sagor.fatloss.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sagor.fatloss.ui.PlanCard
import com.sagor.fatloss.ui.ProgressLine
import com.sagor.fatloss.ui.Section
import com.sagor.fatloss.viewmodel.StepsViewModel

@Composable
fun StepsScreen(vm: StepsViewModel) {
    val state by vm.state.collectAsState()
    DisposableEffect(Unit) {
        vm.start()
        onDispose { vm.stop() }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp)) {
        Section("Step & Walk Tracking")
        PlanCard("Today steps", if (state.sensorAvailable) "Using Android StepCounter sensor." else "No step sensor found on this device.") {
            ProgressLine("Daily goal", state.steps / state.goal.toFloat(), "${state.steps}/${state.goal} steps")
        }
        PlanCard("Morning walk rule", "30 min fasted walk first. Water only before it. Walk counts toward the daily step goal.")
    }
}
