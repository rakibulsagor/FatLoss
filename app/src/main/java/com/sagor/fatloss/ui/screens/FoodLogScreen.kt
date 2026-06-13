package com.sagor.fatloss.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sagor.fatloss.data.FoodSuggestion
import com.sagor.fatloss.data.PlanData
import com.sagor.fatloss.ui.Blue
import com.sagor.fatloss.ui.Coral
import com.sagor.fatloss.ui.Green
import com.sagor.fatloss.ui.Muted
import com.sagor.fatloss.ui.PlanCard
import com.sagor.fatloss.ui.PrimaryAction
import com.sagor.fatloss.ui.ProgressLine
import com.sagor.fatloss.ui.Section
import com.sagor.fatloss.ui.Surface2
import com.sagor.fatloss.ui.TextColor
import com.sagor.fatloss.ui.TopBar
import com.sagor.fatloss.viewmodel.FoodViewModel

private val extraFoods = listOf(
    FoodSuggestion("Khichuri", 420, 16),
    FoodSuggestion("Chicken curry", 280, 30),
    FoodSuggestion("Egg bhuna", 180, 13),
    FoodSuggestion("Chola", 220, 12),
    FoodSuggestion("Chicken ruti roll", 360, 25),
    FoodSuggestion("Tuna/egg sandwich", 330, 24),
    FoodSuggestion("Doi + banana", 230, 10),
    FoodSuggestion("Milk tea no sugar", 40, 2)
)

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun FoodLogScreen(vm: FoodViewModel, onProfileClick: () -> Unit = {}) {
    val state by vm.state.collectAsState()
    var meal by remember { mutableStateOf("Breakfast") }
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    val totalCalories = state.foods.sumOf { it.calories }
    val totalProtein = state.foods.sumOf { it.protein }

    fun addSuggestion(type: String, item: FoodSuggestion) {
        vm.add(type, item.name, item.calories.toString(), item.protein.toString())
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar("Fat Loss", "7", onProfileClick = onProfileClick)
        Column(Modifier.padding(16.dp)) {
            Section("Daily Summary")
            PlanCard("Daily Summary", "Today", accent = Green) {
                ProgressLine("Calories", totalCalories / state.profile.calorieTarget.toFloat(), "$totalCalories/${state.profile.calorieTarget} kcal")
                ProgressLine("Protein", totalProtein / state.profile.proteinTarget.toFloat(), "$totalProtein/${state.profile.proteinTarget} g")
            }

            Section("Fixed Meal Plan")
            MealPlanButton("Breakfast", "2 Ruti\n2-3 Eggs\nShobji", "~350 kcal", Green) {
                vm.add("Breakfast", "Fixed breakfast: 2 ruti, eggs, shobji", "350", "24")
            }
            MealPlanButton("Lunch", "1 Cup Rice\nFish / Meat\nDal & Shobji", "~500 kcal", Blue) {
                vm.add("Lunch", "Fixed lunch: rice, fish/meat, dal, shobji", "500", "35")
            }
            MealPlanButton("Dinner", "2 Ruti\nDal\nShobji", "ZERO RICE", Coral) {
                vm.add("Dinner", "Fixed dinner: ruti, dal, shobji", "300", "18")
            }

            Section("Log Food")
            PlanCard("Add meal", "", accent = Green) {
                MealDropdown(meal = meal, onMeal = { meal = it })
                OutlinedTextField(name, { name = it }, label = { Text("Meal / item name") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        calories,
                        { calories = it.filter(Char::isDigit).take(4) },
                        label = { Text("Calories") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        protein,
                        { protein = it.filter(Char::isDigit).take(3) },
                        label = { Text("Protein") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                PrimaryAction("Add Entry") {
                    vm.add(meal, name, calories, protein)
                    name = ""; calories = ""; protein = ""
                }
            }

            Section("Quick Add Common Items")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (PlanData.foodSuggestions + extraFoods).forEach { item ->
                    QuickFoodChip(item) { addSuggestion(meal, item) }
                }
            }

            Section("Today's Food")
            state.foods.forEach { entry ->
                PlanCard("${entry.mealType}: ${entry.name}", "${entry.calories} kcal | ${entry.protein}g protein", accent = Green) {
                    Button(
                        onClick = { vm.delete(entry.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Surface2, contentColor = TextColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Text("Delete", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MealPlanButton(title: String, body: String, badge: String, color: androidx.compose.ui.graphics.Color, onLog: () -> Unit) {
    PlanCard(title, body, accent = color) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(badge, color = color, fontWeight = FontWeight.Black)
            OutlinedButton(onClick = onLog, border = BorderStroke(1.dp, color), shape = RoundedCornerShape(8.dp)) {
                Text("Log Plan", color = TextColor)
            }
        }
    }
}

@Composable
private fun MealDropdown(meal: String, onMeal: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
        Text("Meal type: $meal", color = TextColor)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach {
            DropdownMenuItem(text = { Text(it) }, onClick = { onMeal(it); expanded = false })
        }
    }
}

@Composable
private fun QuickFoodChip(item: FoodSuggestion, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = {
            Row {
                Text(item.name, color = TextColor, fontWeight = FontWeight.Black)
                Text("  |  ${item.calories} kcal", color = Muted, fontWeight = FontWeight.Bold)
            }
        },
        border = BorderStroke(1.dp, Green.copy(alpha = .35f)),
        shape = RoundedCornerShape(22.dp)
    )
}
