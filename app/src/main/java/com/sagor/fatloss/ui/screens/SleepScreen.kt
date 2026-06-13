package com.sagor.fatloss.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sagor.fatloss.data.displayDate
import com.sagor.fatloss.ui.AppTextField
import com.sagor.fatloss.ui.PlanCard
import com.sagor.fatloss.ui.PrimaryAction
import com.sagor.fatloss.ui.Section
import com.sagor.fatloss.viewmodel.SleepViewModel

@Composable
fun SleepScreen(vm: SleepViewModel) {
    val history by vm.history.collectAsState()
    var bed by remember { mutableStateOf("1:00 AM") }
    var wake by remember { mutableStateOf("8:00 AM") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp)) {
        Section("Sleep Tracking")
        PlanCard("Sleep shift target", "Weeks 1-2: 1:00 AM\nWeeks 3-4: 12:00 AM\nWeeks 5-6: 11:00 PM\nWeek 7+: 11:00 PM natural rhythm")
        PlanCard("Log sleep", "Use format like 1:00 AM and 8:00 AM.") {
            AppTextField(bed, "Bedtime", { bed = it })
            AppTextField(wake, "Wake time", { wake = it })
            PrimaryAction("Save sleep") { vm.add(bed, wake) }
        }
        Section("Sleep History")
        history.forEach {
            PlanCard(displayDate(it.date), "${it.bedtime} to ${it.wakeTime}\n%.1f hours".format(it.hours))
        }
    }
}
