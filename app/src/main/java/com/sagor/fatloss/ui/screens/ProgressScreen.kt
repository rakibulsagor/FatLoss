package com.sagor.fatloss.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sagor.fatloss.ui.Blue
import com.sagor.fatloss.ui.Bg
import com.sagor.fatloss.ui.Coral
import com.sagor.fatloss.ui.Green
import com.sagor.fatloss.ui.Muted
import com.sagor.fatloss.ui.Outline
import com.sagor.fatloss.ui.Red
import com.sagor.fatloss.ui.ProgressLine
import com.sagor.fatloss.ui.Surface
import com.sagor.fatloss.ui.Surface2
import com.sagor.fatloss.ui.Surface3
import com.sagor.fatloss.ui.TextColor
import com.sagor.fatloss.viewmodel.ProgressState
import com.sagor.fatloss.viewmodel.ProgressViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProgressScreen(vm: ProgressViewModel, onProfileClick: () -> Unit = {}) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    var weight by remember { mutableStateOf("") }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val file = File(context.filesDir, "progress_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            vm.addPhoto(file.absolutePath)
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 7 })
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("DASHBOARD", color = Muted.copy(alpha = .65f), fontSize = 12.sp, letterSpacing = 5.sp, fontWeight = FontWeight.Black)
                Text("Progress", color = TextColor, style = MaterialTheme.typography.headlineLarge)
                Box(Modifier.width(70.dp).height(4.dp).background(Green, RoundedCornerShape(4.dp)))

                StreakHero(state)
                ActivityGrid(state)
                BmiAndTargetCard(state)
                DescentMilestonesCard(state)
                PhaseFoundationCard()
                CalendarCard(state)
                WorkoutsPerMonthCard(state)
                BadgeHeader(state)
                BadgeList(state)
                FunctionalProgressActions(
                    weight = weight,
                    onWeight = { weight = it },
                    onSaveWeight = {
                        vm.addWeight(weight)
                        weight = ""
                    },
                    onPhoto = { photoPicker.launch("image/*") }
                )
                VisualEvidenceCard(state)
            }
        }
    }
}

@Composable
private fun BmiAndTargetCard(state: ProgressState) {
    val currentBmi = state.profile.currentWeightKg / ((state.profile.heightCm / 100.0) * (state.profile.heightCm / 100.0))
    val targetBmi = state.profile.targetWeightKg / ((state.profile.heightCm / 100.0) * (state.profile.heightCm / 100.0))
    GlowCard(accent = Blue) {
        BmiBlock("BMI Calculator", currentBmi, "Overweight", Red)
        Spacer(Modifier.height(14.dp))
        BmiBlock("Target BMI", targetBmi, "Normal", Blue)
    }
}

@Composable
private fun BmiBlock(title: String, bmi: Double, status: String, statusColor: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text(title, color = TextColor, style = MaterialTheme.typography.titleMedium)
        Box(Modifier.background(statusColor.copy(alpha = .75f), RoundedCornerShape(6.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(status, color = TextColor, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
    }
    Text("%.1f kg/m2".format(bmi), color = TextColor, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 8.dp))
    Row(Modifier.fillMaxWidth().height(12.dp).padding(top = 6.dp)) {
        Box(Modifier.weight(.25f).height(10.dp).background(Blue, RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)))
        Box(Modifier.weight(.5f).height(10.dp).background(Green))
        Box(Modifier.weight(.25f).height(10.dp).background(Red, RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)))
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("18.5", color = Muted, fontSize = 11.sp)
        Text("25.0", color = Muted, fontSize = 11.sp)
        Text("30.0", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun DescentMilestonesCard(state: ProgressState) {
    val current = state.profile.currentWeightKg
    val milestones = listOf(80, 77, 74, 65)
    GlowCard(accent = Green) {
        Text("Descent Milestones", color = TextColor, style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            milestones.forEach { kg ->
                val done = current <= kg
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .background(if (done) Green else Surface3, RoundedCornerShape(20.dp))
                            .border(4.dp, if (done) Green else Surface3, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (kg == 65) Icons.Default.Flag else Icons.Default.EmojiEvents, contentDescription = null, tint = if (done) Bg else Muted)
                    }
                    Text("${kg}kg", color = if (done) Green else TextColor, fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun PhaseFoundationCard() {
    Card(colors = CardDefaults.cardColors(containerColor = Green), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(22.dp)) {
            Box(Modifier.background(Bg.copy(alpha = .25f), RoundedCornerShape(20.dp)).padding(horizontal = 14.dp, vertical = 7.dp)) {
                Text("ACTIVE PHASE", color = Bg.copy(alpha = .7f), fontWeight = FontWeight.Black)
            }
            Text("Phase 1: Foundation", color = Bg.copy(alpha = .72f), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 16.dp))
            Text("Establishing base discipline. No complex rules, just execution.", color = Bg.copy(alpha = .65f), fontSize = 18.sp, modifier = Modifier.padding(top = 10.dp))
            PhaseTask("Fix Sleep Schedule")
            PhaseTask("No Dinner Rice")
        }
    }
}

@Composable
private fun PhaseTask(text: String) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp).background(Bg.copy(alpha = .10f), RoundedCornerShape(10.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("✓", color = Bg.copy(alpha = .6f), fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text(text, color = Bg.copy(alpha = .65f), fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun StreakHero(state: ProgressState) {
    GlowCard(accent = Green, modifier = Modifier.height(210.dp)) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("I", color = Surface3, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text("🔥", fontSize = 28.sp)
                Text(state.currentStreak.toString(), color = TextColor, fontSize = 60.sp, fontWeight = FontWeight.Black)
                Text("days", color = Muted, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text("CURRENT STREAK", color = Green, fontSize = 12.sp, letterSpacing = 5.sp, fontWeight = FontWeight.Black)
                Text(
                    if (state.currentStreak == 0) "Time to\nget started" else "Keep the\nfire alive",
                    color = TextColor,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(Surface3).padding(top = 12.dp))
                Text(if (state.currentStreak == 0) "Start today!" else "Never miss twice.", color = Muted, modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}

@Composable
private fun ActivityGrid(state: ProgressState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GlowCard(accent = Green, modifier = Modifier.weight(1.2f).height(210.dp)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                IconBadge(Icons.Default.FitnessCenter, Green)
                Column {
                    Text("ACTIVITY", color = Green, fontSize = 12.sp, letterSpacing = 5.sp, fontWeight = FontWeight.Black)
                    Text(state.totalSessions.toString(), color = Green, fontSize = 58.sp, fontWeight = FontWeight.Black)
                    Text("total sessions", color = Muted, fontWeight = FontWeight.Bold)
                }
            }
        }
        Column(Modifier.weight(.85f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniMetric(Icons.Default.TrackChanges, "%.1f".format(state.kmWalked), "km walked", Blue)
            MiniMetric(Icons.Default.FitnessCenter, state.cardioMinutes.toString(), "min cardio", Green)
            MiniMetric(Icons.Default.EmojiEvents, state.goalPerWeek.toString(), "goal / week", Coral)
        }
    }
}

@Composable
private fun CalendarCard(state: ProgressState) {
    val month = YearMonth.now()
    GlowCard(accent = Blue) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Default.DateRange, Green)
            Text(
                "${month.month.getDisplayName(TextStyle.FULL, Locale.US)} ${month.year}",
                color = TextColor,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 10.dp).weight(1f)
            )
            Box(
                Modifier
                    .background(Green.copy(alpha = .15f), RoundedCornerShape(18.dp))
                    .border(1.dp, Green.copy(alpha = .45f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text("${state.activeDaysThisMonth} active days", color = Green, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
        MonthGrid(month = month, activeDates = state.activeDates)
    }
}

@Composable
private fun MonthGrid(month: YearMonth, activeDates: Set<String>) {
    val first = month.atDay(1)
    val startOffset = first.dayOfWeek.value - 1
    val days = month.lengthOfMonth()
    val cells = startOffset + days
    val rows = (cells + 6) / 7
    Column(Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(it, color = Muted.copy(alpha = .65f), fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            }
        }
        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(7) { col ->
                    val index = row * 7 + col
                    val day = index - startOffset + 1
                    val date = if (day in 1..days) month.atDay(day) else null
                    val active = date?.toString()?.let { activeDates.contains(it) } == true
                    val today = date == LocalDate.now()
                    Box(
                        Modifier
                            .weight(1f)
                            .height(42.dp)
                            .background(if (active) Green.copy(alpha = .18f) else Color.Transparent, RoundedCornerShape(10.dp))
                            .border(
                                width = if (today) 2.dp else 1.dp,
                                color = when {
                                    today -> Green
                                    active -> Green.copy(alpha = .55f)
                                    else -> Color.Transparent
                                },
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) Text(day.toString(), color = if (active || today) TextColor else Muted, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutsPerMonthCard(state: ProgressState) {
    GlowCard(accent = Green, modifier = Modifier.height(220.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Default.BarChart, Green)
            Text("Workouts per month", color = TextColor, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 10.dp))
        }
        if (state.monthlyWorkoutCounts.all { it.second == 0 }) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📊", fontSize = 36.sp)
                    Text("No data yet", color = Muted, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            MonthlyBarChart(state.monthlyWorkoutCounts)
        }
    }
}

@Composable
private fun MonthlyBarChart(values: List<Pair<YearMonth, Int>>) {
    val max = values.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
    Row(Modifier.fillMaxSize().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
        values.forEach { (month, count) ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height((24 + 96 * (count / max.toFloat())).dp)
                        .background(if (count > 0) Green else Surface3, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                )
                Text(month.month.name.take(3), color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun BadgeHeader(state: ProgressState) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Badges", color = TextColor, style = MaterialTheme.typography.headlineMedium)
            Text("${unlockedBadgeCount(state)}/9 Unlocked!", color = Muted, fontWeight = FontWeight.Bold)
        }
        Box(
            Modifier
                .background(Surface, RoundedCornerShape(24.dp))
                .border(1.dp, Coral.copy(alpha = .45f), RoundedCornerShape(24.dp))
                .padding(horizontal = 28.dp, vertical = 13.dp)
        ) {
            Text("${(unlockedBadgeCount(state) / 9f * 100).toInt()}%", color = Coral, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun BadgeList(state: ProgressState) {
    Text("• IN PROGRESS", color = Muted.copy(alpha = .55f), letterSpacing = 5.sp, fontSize = 12.sp, fontWeight = FontWeight.Black)
    GlowCard(accent = Surface3) {
        badgeModels(state).forEachIndexed { index, badge ->
            BadgeRow(badge)
            if (index != badgeModels(state).lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(Surface3))
        }
    }
}

@Composable
private fun BadgeRow(badge: BadgeModel) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(44.dp)
                .background(if (badge.unlocked) Green.copy(alpha = .18f) else Surface2, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(if (badge.unlocked) Icons.Default.EmojiEvents else Icons.Default.Lock, contentDescription = null, tint = if (badge.unlocked) Green else Muted.copy(alpha = .55f))
        }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(badge.title, color = if (badge.unlocked) TextColor else Muted, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text(badge.caption, color = Muted.copy(alpha = .75f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Box(Modifier.width(86.dp)) {
            ProgressLine("", badge.progress, "")
        }
    }
}

@Composable
private fun FunctionalProgressActions(
    weight: String,
    onWeight: (String) -> Unit,
    onSaveWeight: () -> Unit,
    onPhoto: () -> Unit
) {
    GlowCard(accent = Blue) {
        Text("Quick Progress Actions", color = TextColor, style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = weight,
            onValueChange = onWeight,
            label = { Text("Log weight kg") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
            ActionButton(Icons.Default.Save, "Save Weight", Modifier.weight(1f), onSaveWeight)
            ActionButton(Icons.Default.PhotoCamera, "Add Photo", Modifier.weight(1f), onPhoto)
        }
    }
}

@Composable
private fun VisualEvidenceCard(state: ProgressState) {
    GlowCard(accent = Blue) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Visual Evidence", color = TextColor, style = MaterialTheme.typography.titleMedium)
            Text("${state.photoCount} photos", color = Blue, fontWeight = FontWeight.Black)
        }
        if (state.photos.isEmpty()) {
            Text("No progress photos yet. Tap Add Photo above; photos are copied into private app storage.", color = Muted, modifier = Modifier.padding(top = 10.dp))
        } else {
            state.photos.take(4).forEach {
                Text("Saved: ${it.date} • ${it.localPath.substringAfterLast(File.separator)}", color = TextColor, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun MiniMetric(icon: ImageVector, value: String, label: String, accent: Color) {
    GlowCard(accent = accent, modifier = Modifier.height(62.dp)) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, accent)
            Column(Modifier.padding(start = 10.dp)) {
                Text(value, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(label, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector, accent: Color) {
    Box(
        Modifier
            .size(42.dp)
            .background(accent.copy(alpha = .13f), RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent)
    }
}

@Composable
private fun GlowCard(accent: Color, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, accent.copy(alpha = .28f)),
        shape = RoundedCornerShape(28.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = accent.copy(alpha = .08f))
    ) {
        Column(Modifier.padding(20.dp), content = content)
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Bg),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(50.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
}

private data class BadgeModel(
    val title: String,
    val caption: String,
    val progress: Float,
    val unlocked: Boolean
)

private fun badgeModels(state: ProgressState): List<BadgeModel> = listOf(
    badge("First Workout", state.totalSessions, 1, ""),
    badge("Week on Fire", state.currentStreak, 7, " d"),
    badge("Month of Steel", state.currentStreak, 30, " d"),
    badge("Regular", state.totalSessions, 10, ""),
    badge("Determined", state.totalSessions, 50, ""),
    badge("Legend", state.totalSessions, 100, ""),
    badge("Walker", state.kmWalked, 10.0, " km"),
    badge("Marathon", state.kmWalked, 50.0, " km"),
    badge("Consistent", state.activeDaysThisMonth, 20, " active")
)

private fun badge(title: String, value: Int, target: Int, suffix: String): BadgeModel =
    BadgeModel(title, "$value/$target$suffix", value / target.toFloat(), value >= target)

private fun badge(title: String, value: Double, target: Double, suffix: String): BadgeModel =
    BadgeModel(title, "%.1f/%.0f%s".format(value, target, suffix), (value / target).toFloat(), value >= target)

private fun unlockedBadgeCount(state: ProgressState): Int = badgeModels(state).count { it.unlocked }
