package com.sagor.fatloss

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.lifecycleScope
import com.sagor.fatloss.data.AppDatabase
import com.sagor.fatloss.data.FatLossRepository
import com.sagor.fatloss.data.today
import com.sagor.fatloss.health.HealthConnectManager
import com.sagor.fatloss.notifications.ReminderScheduler
import com.sagor.fatloss.sensors.StepCounterManager
import com.sagor.fatloss.ui.AppNav
import com.sagor.fatloss.ui.LocalAppGraph
import com.sagor.fatloss.ui.SagorTheme
import com.sagor.fatloss.wellbeing.WellbeingManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    private var graph: AppGraph? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.get(this)
        val repository = FatLossRepository(database.dao())
        graph = AppGraph(
            repository = repository,
            stepCounter = StepCounterManager(this, repository),
            reminders = ReminderScheduler(this),
            healthConnect = HealthConnectManager(this),
            wellbeing = WellbeingManager(this)
        )

        requestUsefulPermissions()

        setContent {
            CompositionLocalProvider(LocalAppGraph provides graph!!) {
                SagorTheme {
                    AppNav()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        graph?.stepCounter?.start()
        lifecycleScope.launch {
            graph?.repository?.markAppUsed(today())
        }
    }

    override fun onStop() {
        graph?.stepCounter?.stop()
        super.onStop()
    }

    private fun requestUsefulPermissions() {
        val permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) add(Manifest.permission.ACTIVITY_RECOGNITION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (permissions.isNotEmpty()) permissionLauncher.launch(permissions.toTypedArray())
    }
}

data class AppGraph(
    val repository: FatLossRepository,
    val stepCounter: StepCounterManager,
    val reminders: ReminderScheduler,
    val healthConnect: HealthConnectManager,
    val wellbeing: WellbeingManager
)
