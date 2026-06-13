package com.sagor.fatloss.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sagor.fatloss.data.PlanData
import com.sagor.fatloss.ui.Amber
import com.sagor.fatloss.ui.Green
import com.sagor.fatloss.ui.PlanCard
import com.sagor.fatloss.ui.Section
import com.sagor.fatloss.viewmodel.RoadmapViewModel

@Composable
fun RoadmapScreen(vm: RoadmapViewModel) {
    val profile by vm.profile.collectAsState()
    val current = PlanData.currentPhase(profile.startDate)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp)) {
        Section("Phase Roadmap")
        PlanData.phases.forEach { phase ->
            val active = phase.number == current
            PlanCard(
                "Phase ${phase.number}: ${phase.title} ${if (active) "(current)" else ""}",
                "${phase.weeks}\n${phase.goals.joinToString("\n") { "- $it" }}\n${phase.expected}",
                accent = if (active) Green else Amber
            )
        }
    }
}
