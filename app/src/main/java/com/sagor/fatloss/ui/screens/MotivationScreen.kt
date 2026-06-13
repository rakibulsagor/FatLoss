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
import com.sagor.fatloss.ui.CheckRow
import com.sagor.fatloss.ui.PlanCard
import com.sagor.fatloss.ui.Red
import com.sagor.fatloss.ui.Section
import com.sagor.fatloss.viewmodel.MotivationViewModel

@Composable
fun MotivationScreen(vm: MotivationViewModel) {
    val task by vm.task.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp)) {
        Section("Motivation & Rules")
        PlanCard("The #1 rule", "Never miss twice in a row. One miss is recovery. Two misses is a pattern. When in doubt, do the minimum.")
        PlanCard("No zero days", "Workout minimum: 10 push-ups + 10 squats\nWalk minimum: 15 min slow walk\nDiet minimum: get protein in")
        PlanCard("Three real enemies", "", accent = Red) {
            CheckRow("Broken sleep avoided", task.brokenSleepAvoided) { vm.toggle("sleep", it) }
            CheckRow("Dinner rice avoided", task.dinnerRiceAvoided) { vm.toggle("rice", it) }
            CheckRow("Late night phone avoided", task.latePhoneAvoided) { vm.toggle("phone", it) }
        }
        PlanCard("Diet rules", "Calories: 1800 max\nProtein: 120g minimum\nWater: 3-3.5 liters\nRice: lunch only, zero dinner rice\nOil: max 2 tbsp/day\nSugar drinks: zero\nFried food: max once per week\nLast meal: done by 9:30 PM")
        PlanCard("Identity shift", "Stop saying: I am trying to lose weight.\nStart saying: I am someone who walks every morning and eats clean.")
    }
}
