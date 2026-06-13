package com.sagor.fatloss.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sagor.fatloss.data.ExercisePlan
import com.sagor.fatloss.data.PlanData
import com.sagor.fatloss.data.WorkoutExerciseEntity
import com.sagor.fatloss.data.WorkoutPlan
import com.sagor.fatloss.ui.Blue
import com.sagor.fatloss.ui.Bg
import com.sagor.fatloss.ui.Green
import com.sagor.fatloss.ui.Muted
import com.sagor.fatloss.ui.Outline
import com.sagor.fatloss.ui.ProgressLine
import com.sagor.fatloss.ui.Red
import com.sagor.fatloss.ui.Surface
import com.sagor.fatloss.ui.Surface2
import com.sagor.fatloss.ui.Surface3
import com.sagor.fatloss.ui.TextColor
import com.sagor.fatloss.ui.TopBar
import com.sagor.fatloss.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun WorkoutScreen(vm: WorkoutViewModel, onProfileClick: () -> Unit = {}) {
    val progress by vm.progress.collectAsState()
    val changes by vm.exerciseChanges.collectAsState()
    val doneKeys = progress.filter { it.completed }.map { it.exerciseName }.toSet()
    var selectedDay by remember { mutableStateOf(LocalDate.now().dayOfWeek) }
    var timer by remember { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<WorkoutExerciseUi?>(null) }
    var showNewExercise by remember { mutableStateOf(false) }

    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            timer -= 1
        }
    }

    val selectedWorkout = PlanData.workouts.first { it.day == selectedDay }
    val selectedExercises = mergedExercises(selectedWorkout, changes)
    val completion = if (selectedExercises.isEmpty()) 0f else {
        selectedExercises.count { doneKeys.contains(it.key) } / selectedExercises.size.toFloat()
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar("Fat Loss", onProfileClick = onProfileClick)
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            WorkoutHeader(selectedWorkout)
            WeeklySchedule(
                selectedDay = selectedDay,
                doneKeys = doneKeys,
                changes = changes,
                onSelect = { selectedDay = it }
            )
            WorkoutDetail(
                workout = selectedWorkout,
                exercises = selectedExercises,
                doneKeys = doneKeys,
                completion = completion,
                timer = timer,
                onToggle = { exercise, done -> vm.setExercise(exercise.key, done) },
                onRest = { timer = it },
                onAdd = { showNewExercise = true },
                onEdit = { editing = it },
                onDelete = { vm.deleteExercise(it.toEntity(deleted = true)) },
                onCopy = { exercise ->
                    val nextDay = nextTrainingDay(selectedDay)
                    vm.saveExercise(exercise.toEntity(day = nextDay, asCustom = true))
                },
                onReset = { vm.resetDay(selectedExercises.map { it.key }) },
                onFinish = { selectedExercises.forEach { vm.setExercise(it.key, true) } }
            )
        }
    }

    if (showNewExercise) {
        ExerciseEditorDialog(
            title = "Add Exercise",
            day = selectedDay,
            start = null,
            orderIndex = selectedExercises.size + 100,
            onDismiss = { showNewExercise = false },
            onSave = {
                vm.saveExercise(it)
                showNewExercise = false
            },
            onDelete = null
        )
    }

    editing?.let { exercise ->
        ExerciseEditorDialog(
            title = "Edit Exercise",
            day = selectedDay,
            start = exercise,
            orderIndex = exercise.orderIndex,
            onDismiss = { editing = null },
            onSave = {
                vm.saveExercise(it)
                editing = null
            },
            onDelete = {
                vm.deleteExercise(exercise.toEntity(deleted = true))
                editing = null
            }
        )
    }
}

@Composable
private fun WorkoutHeader(workout: WorkoutPlan) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Workout Schedule",
            color = TextColor,
            fontFamily = FontFamily.SansSerif,
            fontSize = 42.sp,
            lineHeight = 46.sp,
            fontWeight = FontWeight.Black
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Week 1", color = Muted, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(10.dp))
            Chip("#${tagFor(workout)}", if (workout.type == "HIIT") Blue else Green)
        }
    }
}

@Composable
private fun WeeklySchedule(
    selectedDay: DayOfWeek,
    doneKeys: Set<String>,
    changes: List<WorkoutExerciseEntity>,
    onSelect: (DayOfWeek) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PlanData.workouts.forEach { workout ->
            val selected = workout.day == selectedDay
            val exercises = mergedExercises(workout, changes)
            val complete = exercises.isNotEmpty() && exercises.all { doneKeys.contains(it.key) }
            val completedCount = exercises.count { doneKeys.contains(it.key) }
            val accent = when {
                selected && workout.type == "HIIT" -> Blue
                selected -> Green
                workout.type == "Recovery" || workout.type == "Rest" -> Outline
                else -> Green.copy(alpha = .45f)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (selected) accent else Surface3),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(workout.day) }
            ) {
                Row(
                    Modifier
                        .border(2.dp, accent, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(52.dp)
                            .background(if (selected) accent.copy(alpha = .16f) else Surface2, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(workout.day.name.take(1), color = if (selected) accent else TextColor, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    }
                    Column(Modifier.padding(start = 14.dp).weight(1f)) {
                        Text(workout.title, color = if (workout.exercises.isEmpty()) Muted else TextColor, style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Chip("#${tagFor(workout)}", if (selected) accent else Muted)
                            if (exercises.isNotEmpty()) {
                                Chip("$completedCount/${exercises.size}", if (complete) Green else Muted)
                            }
                        }
                    }
                    Text(if (complete) "DONE" else if (selected) "TODAY" else ">", color = accent, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun WorkoutDetail(
    workout: WorkoutPlan,
    exercises: List<WorkoutExerciseUi>,
    doneKeys: Set<String>,
    completion: Float,
    timer: Int,
    onToggle: (WorkoutExerciseUi, Boolean) -> Unit,
    onRest: (Int) -> Unit,
    onAdd: () -> Unit,
    onEdit: (WorkoutExerciseUi) -> Unit,
    onDelete: (WorkoutExerciseUi) -> Unit,
    onCopy: (WorkoutExerciseUi) -> Unit,
    onReset: () -> Unit,
    onFinish: () -> Unit
) {
    val accent = if (workout.type == "HIIT") Blue else Green
    val completedCount = exercises.count { doneKeys.contains(it.key) }
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, accent.copy(alpha = .5f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Chip("#${workout.type.uppercase()}", accent)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onAdd) { Icon(Icons.Default.Add, contentDescription = "Add exercise", tint = Green) }
                IconButton(onClick = onReset) { Icon(Icons.Default.RestartAlt, contentDescription = "Reset day", tint = Muted) }
            }
            Text("${workout.day.name.lowercase().replaceFirstChar { it.uppercase() }}: ${workout.title}", color = TextColor, style = MaterialTheme.typography.headlineMedium)
            Text(descriptionFor(workout), color = Muted, fontSize = 14.sp)
            WorkoutStatsRow(
                exerciseCount = exercises.size,
                completedCount = completedCount,
                estimatedMinutes = estimatedWorkoutMinutes(exercises),
                totalRestSeconds = exercises.sumOf { it.restSeconds }
            )

            if (timer > 0) {
                TimerPanel(
                    timer = timer,
                    accent = accent,
                    onAddTime = { onRest(timer + 30) },
                    onSkip = { onRest(0) }
                )
            }

            if (exercises.isEmpty()) {
                Text("Recovery day. Add mobility work, stretching, or an easy walk if needed.", color = Muted)
            } else {
                exercises.forEach { exercise ->
                    ExerciseRow(
                        exercise = exercise,
                        checked = doneKeys.contains(exercise.key),
                        accent = accent,
                        onToggle = { onToggle(exercise, it) },
                        onRest = { onRest(exercise.restSeconds) },
                        onEdit = { onEdit(exercise) },
                        onDelete = { onDelete(exercise) },
                        onCopy = { onCopy(exercise) }
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("WORKOUT PROGRESS", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text("${(completion * 100).toInt()}%", color = Green, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
            ProgressLine("Complete", completion, "$completedCount/${exercises.size} exercises")
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Bg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(58.dp)
            ) {
                Text("FINISH WORKOUT", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun WorkoutStatsRow(
    exerciseCount: Int,
    completedCount: Int,
    estimatedMinutes: Int,
    totalRestSeconds: Int
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatPill("Done", "$completedCount/$exerciseCount", Green, Modifier.weight(1f))
        StatPill("Time", "${estimatedMinutes}m", Blue, Modifier.weight(1f))
        StatPill("Rest", formatTimer(totalRestSeconds), Muted, Modifier.weight(1f))
    }
}

@Composable
private fun StatPill(label: String, value: String, accent: Color, modifier: Modifier) {
    Column(
        modifier
            .background(Surface2, RoundedCornerShape(8.dp))
            .border(1.dp, accent.copy(alpha = .35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(label.uppercase(), color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Text(value, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ExerciseRow(
    exercise: WorkoutExerciseUi,
    checked: Boolean,
    accent: Color,
    onToggle: (Boolean) -> Unit,
    onRest: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface2),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (checked) Green.copy(alpha = .8f) else Surface3),
        modifier = Modifier.fillMaxWidth().alpha(if (checked) .72f else 1f)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(30.dp)
                        .background(if (checked) Green else Color.Transparent, RoundedCornerShape(15.dp))
                        .border(2.dp, if (checked) Green else Outline, RoundedCornerShape(15.dp))
                        .clickable { onToggle(!checked) },
                    contentAlignment = Alignment.Center
                ) {
                    if (checked) Text("OK", color = Bg, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(
                        exercise.name,
                        color = TextColor,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (checked) TextDecoration.LineThrough else null
                    )
                    Text(exercise.prescription, color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Text("${exercise.restSeconds}s", color = accent, fontWeight = FontWeight.Black)
            }
            Text(exercise.instruction, color = Muted, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                SmallAction(Icons.Default.FitnessCenter, "Rest", Modifier.weight(1f), onRest)
                SmallAction(Icons.Default.Edit, "Edit", Modifier.weight(1f), onEdit)
                SmallAction(Icons.Default.ContentCopy, "Copy", Modifier.weight(1f), onCopy)
                SmallAction(Icons.Default.Delete, "Delete", Modifier.weight(1f), onDelete, Red)
            }
        }
    }
}

@Composable
private fun SmallAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier,
    onClick: () -> Unit,
    tint: Color = Green
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Surface3, contentColor = tint),
        shape = RoundedCornerShape(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp),
        modifier = modifier.height(40.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun TimerPanel(
    timer: Int,
    accent: Color,
    onAddTime: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = .12f), RoundedCornerShape(12.dp))
            .border(1.dp, accent.copy(alpha = .65f), RoundedCornerShape(12.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Text("REST TIMER", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        Text(formatTimer(timer), color = TextColor, fontSize = 58.sp, fontWeight = FontWeight.Black)
        Text("Recover, breathe, then hit the next set clean.", color = Muted, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallAction(Icons.Default.Add, "+30s", Modifier.weight(1f), onAddTime, accent)
            SmallAction(Icons.Default.SkipNext, "Skip", Modifier.weight(1f), onSkip, Muted)
        }
    }
}

@Composable
private fun ExerciseEditorDialog(
    title: String,
    day: DayOfWeek,
    start: WorkoutExerciseUi?,
    orderIndex: Int,
    onDismiss: () -> Unit,
    onSave: (WorkoutExerciseEntity) -> Unit,
    onDelete: (() -> Unit)?
) {
    var name by remember(start) { mutableStateOf(start?.name ?: "") }
    var prescription by remember(start) { mutableStateOf(start?.prescription ?: "3 x 12") }
    var rest by remember(start) { mutableIntStateOf(start?.restSeconds ?: 60) }
    var instruction by remember(start) { mutableStateOf(start?.instruction ?: "Keep clean form and stop before pain.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = { Text(title.uppercase(), color = Green, style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prescription,
                    onValueChange = { prescription = it },
                    label = { Text("Sets / reps / duration") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { rest = (rest - 15).coerceAtLeast(15) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease rest", tint = TextColor)
                    }
                    Text(rest.toString(), color = TextColor, fontSize = 42.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 20.dp))
                    IconButton(onClick = { rest += 15 }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase rest", tint = TextColor)
                    }
                }
                OutlinedTextField(
                    value = instruction,
                    onValueChange = { instruction = it },
                    label = { Text("Instruction") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("#Strength", Green)
                    Chip("#Cardio", Blue)
                    Chip("#Core", Muted)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            WorkoutExerciseEntity(
                                id = start?.entity?.id ?: start?.sourceName?.let { builtinId(day, it) } ?: customId(day),
                                day = day.value,
                                sourceName = start?.sourceName,
                                name = name.trim(),
                                prescription = prescription.ifBlank { "3 x 12" },
                                restSeconds = rest,
                                instruction = instruction.ifBlank { "Keep clean form." },
                                orderIndex = orderIndex,
                                deleted = false
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Bg)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Save", modifier = Modifier.padding(start = 6.dp), fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Delete", color = Red) }
                }
                TextButton(onClick = onDismiss) { Text("Cancel", color = Muted) }
            }
        }
    )
}

@Composable
private fun Chip(text: String, color: Color) {
    Box(
        Modifier
            .padding(top = 4.dp)
            .background(color.copy(alpha = .12f), RoundedCornerShape(999.dp))
            .border(1.dp, color.copy(alpha = .45f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

private data class WorkoutExerciseUi(
    val key: String,
    val entity: WorkoutExerciseEntity?,
    val day: DayOfWeek,
    val sourceName: String?,
    val name: String,
    val prescription: String,
    val restSeconds: Int,
    val instruction: String,
    val orderIndex: Int
) {
    fun toEntity(day: DayOfWeek = this.day, asCustom: Boolean = false, deleted: Boolean = false): WorkoutExerciseEntity =
        WorkoutExerciseEntity(
            id = if (asCustom) customId(day) else entity?.id ?: sourceName?.let { builtinId(day, it) } ?: key,
            day = day.value,
            sourceName = if (asCustom) null else sourceName,
            name = name,
            prescription = prescription,
            restSeconds = restSeconds,
            instruction = instruction,
            orderIndex = orderIndex,
            deleted = deleted
        )
}

private fun mergedExercises(workout: WorkoutPlan, changes: List<WorkoutExerciseEntity>): List<WorkoutExerciseUi> {
    val dayChanges = changes.filter { it.day == workout.day.value }
    val overrides = dayChanges.filter { it.sourceName != null }.associateBy { it.sourceName }
    val defaults = workout.exercises.mapIndexedNotNull { index, exercise ->
        val override = overrides[exercise.name]
        if (override?.deleted == true) {
            null
        } else {
            val source = override ?: exercise.toEntity(workout.day, index)
            WorkoutExerciseUi(
                key = exercise.name,
                entity = override,
                day = workout.day,
                sourceName = exercise.name,
                name = source.name,
                prescription = source.prescription,
                restSeconds = source.restSeconds,
                instruction = source.instruction,
                orderIndex = index
            )
        }
    }
    val customs = dayChanges
        .filter { it.sourceName == null && !it.deleted }
        .map {
            WorkoutExerciseUi(
                key = it.id,
                entity = it,
                day = workout.day,
                sourceName = null,
                name = it.name,
                prescription = it.prescription,
                restSeconds = it.restSeconds,
                instruction = it.instruction,
                orderIndex = it.orderIndex
            )
        }
    return (defaults + customs).sortedBy { it.orderIndex }
}

private fun ExercisePlan.toEntity(day: DayOfWeek, orderIndex: Int) = WorkoutExerciseEntity(
    id = builtinId(day, name),
    day = day.value,
    sourceName = name,
    name = name,
    prescription = prescription,
    restSeconds = restSeconds,
    instruction = instruction,
    orderIndex = orderIndex,
    deleted = false
)

private fun descriptionFor(workout: WorkoutPlan): String = when (workout.type) {
    "HIIT" -> "High intensity intervals for calorie burn and cardiovascular endurance. Keep breaks minimal."
    "Recovery", "Rest" -> "Let muscles repair. Keep movement easy and controlled."
    else -> "Bodyweight strength training. Clean reps first, speed second."
}

private fun tagFor(workout: WorkoutPlan): String = when (workout.day) {
    DayOfWeek.MONDAY -> "Strength"
    DayOfWeek.TUESDAY -> "Cardio"
    DayOfWeek.WEDNESDAY -> "Mobility"
    DayOfWeek.THURSDAY -> "LegDay"
    DayOfWeek.FRIDAY -> "Endurance"
    DayOfWeek.SATURDAY -> "HIIT"
    DayOfWeek.SUNDAY -> "LISS"
}

private fun nextTrainingDay(day: DayOfWeek): DayOfWeek {
    val index = PlanData.workouts.indexOfFirst { it.day == day }
    return PlanData.workouts[(index + 1).mod(PlanData.workouts.size)].day
}

private fun estimatedWorkoutMinutes(exercises: List<WorkoutExerciseUi>): Int =
    ((exercises.size * 150) + exercises.sumOf { it.restSeconds }).coerceAtLeast(0) / 60

private fun formatTimer(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)

private fun builtinId(day: DayOfWeek, sourceName: String): String =
    "builtin-${day.value}-${sourceName.lowercase().filter { it.isLetterOrDigit() }}"

private fun customId(day: DayOfWeek): String = "custom-${day.value}-${System.currentTimeMillis()}"

