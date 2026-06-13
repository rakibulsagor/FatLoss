package com.sagor.fatloss.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sagor.fatloss.ui.screens.AnalyticsScreen
import com.sagor.fatloss.ui.screens.DashboardScreen
import com.sagor.fatloss.ui.screens.FoodLogScreen
import com.sagor.fatloss.ui.screens.MotivationScreen
import com.sagor.fatloss.ui.screens.ProgressScreen
import com.sagor.fatloss.ui.screens.RoadmapScreen
import com.sagor.fatloss.ui.screens.SettingsScreen
import com.sagor.fatloss.ui.screens.SleepScreen
import com.sagor.fatloss.ui.screens.StepsScreen
import com.sagor.fatloss.ui.screens.WorkoutScreen
import com.sagor.fatloss.viewmodel.VmFactory

private val routes = listOf(
    NavItem("dashboard", "Today", Icons.Default.Dashboard),
    NavItem("workout", "Workout", Icons.Default.FitnessCenter),
    NavItem("food", "Meals", Icons.Default.Restaurant),
    NavItem("progress", "Progress", Icons.Default.BarChart),
    NavItem("analytics", "Analytics", Icons.Default.Analytics),
    NavItem("settings", "Profile", Icons.Default.Person)
)

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val graph = LocalAppGraph.current
    val factory = VmFactory(graph)
    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        NavHost(navController = nav, startDestination = "dashboard", modifier = Modifier.weight(1f)) {
            composable("dashboard") { DashboardScreen(viewModel(factory = factory)) { nav.navigate("settings") { launchSingleTop = true } } }
            composable("workout") { WorkoutScreen(viewModel(factory = factory)) { nav.navigate("settings") { launchSingleTop = true } } }
            composable("food") { FoodLogScreen(viewModel(factory = factory)) { nav.navigate("settings") { launchSingleTop = true } } }
            composable("progress") { ProgressScreen(viewModel(factory = factory)) { nav.navigate("settings") { launchSingleTop = true } } }
            composable("analytics") { AnalyticsScreen(viewModel(factory = factory)) { nav.navigate("settings") { launchSingleTop = true } } }
            composable("settings") { SettingsScreen(viewModel(factory = factory)) }
        }
        TabBar(nav)
    }
}

@Composable
private fun TabBar(nav: NavHostController) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: "dashboard"
    NavigationBar(
        containerColor = Surface,
        contentColor = TextColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Surface3, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
    ) {
        routes.forEach { item ->
            val selected = item.route == current
            NavigationBarItem(
                selected = selected,
                onClick = { nav.navigate(item.route) { launchSingleTop = true } },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = {
                    Text(
                        item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Black else FontWeight.Bold
                    )
                },
                alwaysShowLabel = true,
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    selectedIconColor = Bg,
                    selectedTextColor = Green,
                    indicatorColor = Green,
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted
                )
            )
        }
    }
}

private data class NavItem(val route: String, val label: String, val icon: ImageVector)
