package com.sagor.fatloss.wellbeing

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class WellbeingManager(private val context: Context) {
    fun usageAccessIntent(): Intent =
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun readToday(): WellbeingSnapshot {
        if (!hasUsageAccess()) {
            return WellbeingSnapshot(
                permissionGranted = false,
                message = "Grant Usage Access to show screen time, unlocks, and top apps."
            )
        }

        val usageManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageManager = context.packageManager
        val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = System.currentTimeMillis()
        val stats = usageManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end).orEmpty()
        val usedApps = stats
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
        val screenMillis = usedApps.sumOf { it.totalTimeInForeground }
        val topApps = usedApps.take(5).map { stat ->
            val label = runCatching {
                val appInfo = packageManager.getApplicationInfo(stat.packageName, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            }.getOrDefault(stat.packageName)
            WellbeingAppUsage(label, stat.packageName, stat.totalTimeInForeground)
        }

        return WellbeingSnapshot(
            permissionGranted = true,
            screenMinutes = TimeUnit.MILLISECONDS.toMinutes(screenMillis),
            unlocks = countUnlocks(usageManager, start, end),
            appCount = usedApps.size,
            topApps = topApps,
            message = "Wellbeing usage synced for today."
        )
    }

    private fun countUnlocks(usageManager: UsageStatsManager, start: Long, end: Long): Int {
        val events = usageManager.queryEvents(start, end) ?: return 0
        val event = UsageEvents.Event()
        var count = 0
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.KEYGUARD_HIDDEN) {
                count++
            }
        }
        return count
    }
}

data class WellbeingSnapshot(
    val permissionGranted: Boolean = false,
    val screenMinutes: Long = 0,
    val unlocks: Int = 0,
    val appCount: Int = 0,
    val topApps: List<WellbeingAppUsage> = emptyList(),
    val message: String = "Not synced yet."
)

data class WellbeingAppUsage(
    val label: String,
    val packageName: String,
    val foregroundMillis: Long
) {
    val minutes: Long get() = TimeUnit.MILLISECONDS.toMinutes(foregroundMillis)
}
