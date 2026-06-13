package com.sagor.fatloss.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.AlarmClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sagor.fatloss.data.UserProfile
import com.sagor.fatloss.ui.Blue
import com.sagor.fatloss.ui.Bg
import com.sagor.fatloss.ui.Green
import com.sagor.fatloss.ui.Muted
import com.sagor.fatloss.ui.Outline
import com.sagor.fatloss.ui.PrimaryAction
import com.sagor.fatloss.ui.Red
import com.sagor.fatloss.ui.Section
import com.sagor.fatloss.ui.Surface
import com.sagor.fatloss.ui.Surface2
import com.sagor.fatloss.ui.Surface3
import com.sagor.fatloss.ui.TextColor
import com.sagor.fatloss.ui.TopBar
import com.sagor.fatloss.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val profile by vm.profile.collectAsState()
    val exportText by vm.exportText.collectAsState()
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    var name by remember(profile.name) { mutableStateOf(profile.name.ifBlank { "Sagor" }) }
    var age by remember(profile.age) { mutableStateOf(profile.age.toString()) }
    var gender by remember(profile.gender) { mutableStateOf(profile.gender) }
    var height by remember(profile.heightCm) { mutableStateOf(profile.heightCm.toString()) }
    var currentWeight by remember(profile.currentWeightKg) { mutableFloatStateOf(profile.currentWeightKg.toFloat()) }
    var targetWeight by remember(profile.targetWeightKg) { mutableFloatStateOf(profile.targetWeightKg.toFloat()) }
    var calories by remember(profile.calorieTarget) { mutableStateOf(profile.calorieTarget.toString()) }
    var protein by remember(profile.proteinTarget) { mutableStateOf(profile.proteinTarget.toString()) }
    var steps by remember(profile.stepGoal) { mutableStateOf(profile.stepGoal.toString()) }
    var startDate by remember(profile.startDate) { mutableStateOf(profile.startDate) }
    var workoutAlarm by remember { mutableStateOf(true) }
    var mealReminder by remember { mutableStateOf(false) }
    var workoutTime by remember { mutableStateOf("06:30") }
    var mealTime by remember { mutableStateOf("20:00") }
    var showFaq by remember { mutableStateOf(false) }
    val prefs = remember(context) {
        context.getSharedPreferences("fat_loss_prefs", android.content.Context.MODE_PRIVATE)
    }
    var profilePhotoPath by remember {
        mutableStateOf(prefs.getString("profile_photo_path", null))
    }
    val profilePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            runCatching {
                val file = File(context.filesDir, "profile_photo.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                prefs.edit().putString("profile_photo_path", file.absolutePath).apply()
                profilePhotoPath = file.absolutePath
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                vm.importJson(reader.readText())
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(120)
        visible = true
    }

    val heightCm = height.toIntOrNull()?.coerceIn(100, 250) ?: profile.heightCm
    val heightMeters = heightCm / 100f
    val bmi = currentWeight / (heightMeters * heightMeters)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar("Fat Loss", "7")
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 })
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ProfileHeader(
                    name = name,
                    photoPath = profilePhotoPath,
                    onNameChange = { name = it },
                    onPickPhoto = { profilePhotoLauncher.launch("image/*") }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ProfileMetricTile(
                        label = "Age",
                        value = age,
                        suffix = "yrs",
                        accent = Blue,
                        modifier = Modifier.weight(1f),
                        onValueChange = { age = it.filter { ch -> ch.isDigit() }.take(2) }
                    )
                    ProfileMetricTile(
                        label = "Gender",
                        value = gender,
                        suffix = "",
                        accent = Green,
                        modifier = Modifier.weight(1f),
                        onValueChange = { gender = it.take(24) }
                    )
                    ProfileMetricTile(
                        label = "Height",
                        value = height,
                        suffix = "cm",
                        accent = Green,
                        modifier = Modifier.weight(1f),
                        onValueChange = { height = it.filter { ch -> ch.isDigit() }.take(3) }
                    )
                }

                WeightGoalCard(
                    current = currentWeight,
                    target = targetWeight,
                    onCurrent = { currentWeight = it },
                    onTarget = { targetWeight = it }
                )

                BmiCard(title = "BMI Calculator", bmi = bmi, status = "Overweight", statusColor = Red)
                BmiCard(
                    title = "Target BMI",
                    bmi = targetWeight / (heightMeters * heightMeters),
                    status = "Normal",
                    statusColor = Blue
                )

                ReminderCard(
                    workoutAlarm = workoutAlarm,
                    mealReminder = mealReminder,
                    workoutTime = workoutTime,
                    mealTime = mealTime,
                    onWorkout = { workoutAlarm = it },
                    onMeal = { mealReminder = it },
                    onWorkoutTime = { workoutTime = it },
                    onMealTime = { mealTime = it },
                    onSetAlarm = { label, time ->
                        val parts = time.split(":")
                        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 6
                        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 30
                        val intent = Intent(AlarmClock.ACTION_SET_ALARM)
                            .putExtra(AlarmClock.EXTRA_MESSAGE, label)
                            .putExtra(AlarmClock.EXTRA_HOUR, hour)
                            .putExtra(AlarmClock.EXTRA_MINUTES, minute)
                        context.startActivity(intent)
                    },
                    onTest = { vm.testReminder() }
                )

                SettingsEditor(
                    calories = calories,
                    protein = protein,
                    steps = steps,
                    startDate = startDate,
                    onCalories = { calories = it.filter { ch -> ch.isDigit() }.take(4) },
                    onProtein = { protein = it.filter { ch -> ch.isDigit() }.take(3) },
                    onSteps = { steps = it.filter { ch -> ch.isDigit() }.take(5) },
                    onStartDate = { startDate = it }
                )

                DeveloperOptions(
                    onGithub = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rakibulsagor")))
                    },
                    onFeedback = {
                        context.startActivity(Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:rakibul.h.sagor@proton.me")
                            putExtra(Intent.EXTRA_SUBJECT, "Fat Loss app feedback")
                        })
                    },
                    onFaq = { showFaq = true },
                    onExportJson = { vm.exportJson() },
                    onExportCsv = { vm.exportCsv() },
                    onImport = { importLauncher.launch("application/json") }
                )

                PrimaryAction("Update Profile") {
                    vm.save(
                        UserProfile(
                            name = name.ifBlank { "Sagor" },
                            age = age.toIntOrNull() ?: profile.age,
                            gender = gender.ifBlank { profile.gender },
                            heightCm = heightCm,
                            startWeightKg = profile.startWeightKg,
                            currentWeightKg = currentWeight.toDouble(),
                            targetWeightKg = targetWeight.toDouble(),
                            calorieTarget = calories.toIntOrNull() ?: profile.calorieTarget,
                            proteinTarget = protein.toIntOrNull() ?: profile.proteinTarget,
                            stepGoal = steps.toIntOrNull() ?: profile.stepGoal,
                            startDate = startDate.ifBlank { profile.startDate }
                        )
                    )
                }

                if (exportText.isNotBlank()) {
                    ProfileCard(accent = Blue) {
                        Text("Export Preview", color = Blue, style = MaterialTheme.typography.titleMedium)
                        Text(exportText.take(1200), color = TextColor, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                        Button(
                            onClick = {
                                context.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND)
                                            .setType("text/plain")
                                            .putExtra(Intent.EXTRA_SUBJECT, "Fat Loss backup")
                                            .putExtra(Intent.EXTRA_TEXT, exportText),
                                        "Share backup"
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                        ) {
                            Text("Share Backup", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    if (showFaq) {
        AlertDialog(
            onDismissRequest = { showFaq = false },
            confirmButton = { TextButton(onClick = { showFaq = false }) { Text("Close") } },
            title = { Text("Fat Loss FAQ") },
            text = {
                Text(
                    "Q: Why no dinner rice?\nA: It protects your calorie deficit at night.\n\n" +
                        "Q: Can I edit today's weight?\nA: Yes, today's entry updates. Older entries are locked.\n\n" +
                        "Q: Where are photos stored?\nA: In private app storage and listed on Progress.\n\n" +
                        "Q: Do alarms work offline?\nA: Yes, they open Android's alarm app."
                )
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    photoPath: String?,
    onNameChange: (String) -> Unit,
    onPickPhoto: () -> Unit
) {
    val bitmap = remember(photoPath) {
        photoPath?.let { runCatching { BitmapFactory.decodeFile(it) }.getOrNull() }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .shadow(20.dp, RoundedCornerShape(52.dp), ambientColor = Green.copy(alpha = .25f))
                .border(2.dp, Green, RoundedCornerShape(52.dp))
                .background(Surface2, RoundedCornerShape(52.dp))
                .clickable { onPickPhoto() },
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(50.dp))
                )
            } else {
                Text("FL", color = Green, fontSize = 30.sp, fontWeight = FontWeight.Black)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(30.dp)
                    .background(Surface3, RoundedCornerShape(15.dp))
                    .border(1.dp, Outline, RoundedCornerShape(15.dp))
                    .clickable { onPickPhoto() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Muted, modifier = Modifier.size(16.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium.copy(color = TextColor),
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.Edit, contentDescription = "Edit name", tint = Muted, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun ProfileMetricTile(
    label: String,
    value: String,
    suffix: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onValueChange: ((String) -> Unit)? = null
) {
    ProfileCard(accent = accent, modifier = modifier.height(106.dp)) {
        Text(label.uppercase(), color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        if (onValueChange != null) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
            )
        } else {
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                if (suffix.isNotBlank()) {
                    Text(suffix, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(start = 3.dp, bottom = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun WeightGoalCard(
    current: Float,
    target: Float,
    onCurrent: (Float) -> Unit,
    onTarget: (Float) -> Unit
) {
    ProfileCard(accent = Green) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = Green)
            Text("Weight Goals", color = TextColor, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
        }
        WeightSlider("Current Weight", current, "kg", onCurrent)
        WeightSlider("Target Weight", target, "kg", onTarget)
    }
}

@Composable
private fun WeightSlider(label: String, value: Float, suffix: String, onValue: (Float) -> Unit) {
    Column(Modifier.padding(top = 14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("%.0f %s".format(value, suffix), color = if (label.startsWith("Target")) Green else TextColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
        Slider(
            value = value,
            onValueChange = onValue,
            valueRange = 40f..150f,
            colors = SliderDefaults.colors(
                thumbColor = Green,
                activeTrackColor = Green,
                inactiveTrackColor = Surface3
            )
        )
    }
}

@Composable
private fun BmiCard(title: String, bmi: Float, status: String, statusColor: Color) {
    ProfileCard(accent = Blue) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = Blue)
                Text(title, color = TextColor, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
            }
            Box(Modifier.background(statusColor.copy(alpha = .75f), RoundedCornerShape(5.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text(status, color = TextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 12.dp)) {
            Text("%.1f".format(bmi), color = Color.White, style = MaterialTheme.typography.headlineLarge)
            Text(" kg/m2", color = Muted, modifier = Modifier.padding(start = 6.dp, bottom = 8.dp))
        }
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
}

@Composable
private fun ReminderCard(
    workoutAlarm: Boolean,
    mealReminder: Boolean,
    workoutTime: String,
    mealTime: String,
    onWorkout: (Boolean) -> Unit,
    onMeal: (Boolean) -> Unit,
    onWorkoutTime: (String) -> Unit,
    onMealTime: (String) -> Unit,
    onSetAlarm: (String, String) -> Unit,
    onTest: () -> Unit
) {
    ProfileCard(accent = Green) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Alarm, contentDescription = null, tint = Green)
            Text("Reminders & Alarms", color = TextColor, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
        }
        ReminderRow("Workout Alarm", workoutTime, Green, workoutAlarm, onWorkout, onWorkoutTime) {
            onSetAlarm("Fat Loss workout", workoutTime)
        }
        ReminderRow("Meal Reminders", mealTime, Blue, mealReminder, onMeal, onMealTime) {
            onSetAlarm("Fat Loss meal reminder", mealTime)
        }
        PrimaryAction("Send Test Reminder", onTest)
    }
}

@Composable
private fun ReminderRow(label: String, time: String, accent: Color, checked: Boolean, onChecked: (Boolean) -> Unit, onTime: (String) -> Unit, onSetAlarm: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .background(Surface2, RoundedCornerShape(8.dp))
            .border(1.dp, accent.copy(alpha = .35f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label.uppercase(), color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = time,
                onValueChange = { onTime(it.take(5)) },
                singleLine = true,
                label = { Text("HH:mm") },
                modifier = Modifier.fillMaxWidth(.65f)
            )
            TextButton(onClick = onSetAlarm) { Text("Set phone alarm") }
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent)
        )
    }
}

@Composable
private fun DeveloperOptions(
    onGithub: () -> Unit,
    onFeedback: () -> Unit,
    onFaq: () -> Unit,
    onExportJson: () -> Unit,
    onExportCsv: () -> Unit,
    onImport: () -> Unit
) {
    ProfileCard(accent = Green) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PersonPin, contentDescription = null, tint = Green)
            Text("Developer Options", color = TextColor, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(start = 8.dp))
        }
        OptionRow(Icons.Default.Link, "Developer Details", "github.com/rakibulsagor", Green, onGithub)
        OptionRow(Icons.Default.ChatBubbleOutline, "Send Feedback", "rakibul.h.sagor@proton.me", Blue, onFeedback)
        OptionRow(Icons.Default.Info, "About Fat Loss", "Version 3.3.0", Green)
        OptionRow(Icons.Default.HelpOutline, "FAQ", "Open help and common questions", Blue, onFaq)
        Text("Data Management", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DataButton(Icons.Default.Upload, "Export JSON", Modifier.weight(1f), onExportJson)
            DataButton(Icons.Default.Download, "Export CSV", Modifier.weight(1f), onExportCsv)
        }
        DataButton(Icons.Default.Download, "Import JSON", Modifier.fillMaxWidth().padding(top = 8.dp), onImport)
    }
}

@Composable
private fun OptionRow(icon: ImageVector, label: String, value: String, accent: Color, onClick: (() -> Unit)? = null) {
    val modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .background(Surface2, RoundedCornerShape(8.dp))
            .border(1.dp, accent.copy(alpha = .35f), RoundedCornerShape(8.dp))
    Row(
        if (onClick != null) modifier.then(Modifier.clickable { onClick() }).padding(12.dp) else modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = accent)
        Column(Modifier.padding(start = 12.dp)) {
            Text(label.uppercase(), color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = TextColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DataButton(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    androidx.compose.material3.Button(
        onClick = onClick,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Surface2, contentColor = TextColor),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Outline),
        modifier = modifier.height(54.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(label, modifier = Modifier.padding(start = 6.dp), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SettingsEditor(
    calories: String,
    protein: String,
    steps: String,
    startDate: String,
    onCalories: (String) -> Unit,
    onProtein: (String) -> Unit,
    onSteps: (String) -> Unit,
    onStartDate: (String) -> Unit
) {
    ProfileCard(accent = Blue) {
        Text("Tracker Settings", color = Blue, style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(calories, onCalories, label = { Text("Calorie target") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(protein, onProtein, label = { Text("Protein target") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(steps, onSteps, label = { Text("Step goal") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(startDate, onStartDate, label = { Text("Start date YYYY-MM-DD") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    }
}

@Composable
private fun ProfileCard(
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Surface3),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .border(2.dp, accent.copy(alpha = .7f), RoundedCornerShape(10.dp))
                .padding(16.dp),
            content = content
        )
    }
}
