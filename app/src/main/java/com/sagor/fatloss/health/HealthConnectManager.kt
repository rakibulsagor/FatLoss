package com.sagor.fatloss.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HealthConnectManager(private val context: Context) {
    val readPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class)
    )

    private var cachedClient: HealthConnectClient? = null

    private val client: HealthConnectClient?
        get() {
            if (sdkStatus() != HealthConnectClient.SDK_AVAILABLE) return null
            return cachedClient ?: HealthConnectClient.getOrCreate(context).also { cachedClient = it }
        }

    fun sdkStatus(): Int = HealthConnectClient.getSdkStatus(context)

    fun providerInstallIntent(): Intent {
        val uri = Uri.parse("market://details?id=com.google.android.apps.healthdata")
        return Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    suspend fun hasAllPermissions(): Boolean {
        val granted = client?.permissionController?.getGrantedPermissions().orEmpty()
        return granted.containsAll(readPermissions)
    }

    suspend fun readToday(): HealthConnectSnapshot {
        val sdk = sdkStatus()
        if (sdk != HealthConnectClient.SDK_AVAILABLE) {
            return HealthConnectSnapshot(
                available = false,
                permissionGranted = false,
                message = if (sdk == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
                    "Health Connect needs to be installed or updated."
                } else {
                    "Health Connect is not available on this phone."
                }
            )
        }

        val healthClient = client ?: return HealthConnectSnapshot(message = "Health Connect client not ready.")
        val permissionGranted = hasAllPermissions()
        if (!permissionGranted) {
            return HealthConnectSnapshot(
                available = true,
                permissionGranted = false,
                message = "Grant Health Connect access to sync steps, calories, sleep, weight, and heart rate."
            )
        }

        val now = Instant.now()
        val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        val range = TimeRangeFilter.between(start, now)
        val aggregate = healthClient.aggregate(
            AggregateRequest(
                metrics = setOf(
                    StepsRecord.COUNT_TOTAL,
                    DistanceRecord.DISTANCE_TOTAL,
                    ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
                    TotalCaloriesBurnedRecord.ENERGY_TOTAL,
                    HeartRateRecord.BPM_AVG,
                    WeightRecord.WEIGHT_AVG
                ),
                timeRangeFilter = range
            )
        )
        val sleepSessions = healthClient.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = range
            )
        ).records
        val sleepMinutes = sleepSessions.sumOf { session ->
            Duration.between(session.startTime, session.endTime).toMinutes()
        }

        return HealthConnectSnapshot(
            available = true,
            permissionGranted = true,
            steps = aggregate[StepsRecord.COUNT_TOTAL] ?: 0L,
            distanceKm = aggregate[DistanceRecord.DISTANCE_TOTAL]?.inKilometers ?: 0.0,
            activeCalories = aggregate[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0,
            totalCalories = aggregate[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0,
            sleepHours = sleepMinutes / 60.0,
            averageHeartRate = aggregate[HeartRateRecord.BPM_AVG]?.toDouble() ?: 0.0,
            weightKg = aggregate[WeightRecord.WEIGHT_AVG]?.inKilograms ?: 0.0,
            lastSync = java.time.LocalTime.now().withSecond(0).withNano(0).toString(),
            message = "Health Connect synced for today."
        )
    }
}

data class HealthConnectSnapshot(
    val available: Boolean = false,
    val permissionGranted: Boolean = false,
    val steps: Long = 0,
    val distanceKm: Double = 0.0,
    val activeCalories: Double = 0.0,
    val totalCalories: Double = 0.0,
    val sleepHours: Double = 0.0,
    val averageHeartRate: Double = 0.0,
    val weightKg: Double = 0.0,
    val lastSync: String = "",
    val message: String = "Not synced yet."
)
