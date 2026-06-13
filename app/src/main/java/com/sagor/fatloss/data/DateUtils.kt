package com.sagor.fatloss.data

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun today(): String = LocalDate.now().toString()

fun displayDate(value: String): String =
    runCatching { LocalDate.parse(value).format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)) }
        .getOrDefault(value)

fun sleepHours(bedtime: String, wakeTime: String): Double {
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    val bed = LocalTime.parse(bedtime.uppercase(Locale.US), formatter)
    var wake = LocalTime.parse(wakeTime.uppercase(Locale.US), formatter)
    var minutes = Duration.between(bed, wake).toMinutes()
    if (minutes <= 0) minutes += 24 * 60
    return minutes / 60.0
}
