package ru.akuzyukhin.orientir.feature.task.domain.util

import android.os.Build
import androidx.annotation.RequiresApi
import ru.akuzyukhin.orientir.feature.task.domain.model.RecurrencePattern
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Парсе и сборщик RRULE-строк */
object RecurrenceRuleParser {

    @RequiresApi(Build.VERSION_CODES.O)
    private val RDATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")

    @RequiresApi(Build.VERSION_CODES.O)
    fun parse(rrule: String): RecurrencePattern {
        val trimmed = rrule.trim()
        if (trimmed.isEmpty()) return RecurrencePattern.Custom(rrule)

        if (trimmed.startsWith("RDATE=")) {
            val value = trimmed.removePrefix("RDATE=")
            return runCatching {
                val date = LocalDate.parse(value, RDATE_FORMAT)
                RecurrencePattern.Once(date)
            }.getOrElse { RecurrencePattern.Custom(rrule) }
        }

        val parts: Map<String, String> = trimmed.split(";")
            .mapNotNull {
                val eq = it.indexOf('=')
                if (eq < 0) null else it.substring(0, eq).trim() to it.substring(eq + 1).trim()
            }
            .toMap()

        val freq = parts["FREQ"] ?: return RecurrencePattern.Custom(rrule)

        return when (freq) {
            "DAILY" -> {
                val interval = parts["INTERVAL"]?.toIntOrNull()
                val knownKeys = setOf("FREQ", "INTERVAL")
                if (parts.keys.any { it !in knownKeys }) {
                    return RecurrencePattern.Custom(rrule)
                }

                when {
                    interval == null || interval == 1 -> RecurrencePattern.Daily
                    interval >= 2 -> RecurrencePattern.EveryNDays(interval)
                    else -> RecurrencePattern.Custom(rrule)
                }
            }

            "WEEKLY" -> {
                val byday = parts["BYDAY"] ?: return RecurrencePattern.Custom(rrule)
                val days = parseByDay(byday) ?: return RecurrencePattern.Custom(rrule)

                val interval = parts["INTERVAL"]?.toIntOrNull()
                if (interval != null && interval != 1) {
                    return RecurrencePattern.Custom(rrule)
                }
                val knownKeys = setOf("FREQ", "BYDAY", "INTERVAL")
                if (parts.keys.any { it !in knownKeys }) {
                    return RecurrencePattern.Custom(rrule)
                }

                RecurrencePattern.Weekly(days)
            }

            else -> RecurrencePattern.Custom(rrule)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun build(pattern: RecurrencePattern, time: LocalTime): String = when (pattern) {
        is RecurrencePattern.Daily -> "FREQ=DAILY"
        is RecurrencePattern.EveryNDays -> "FREQ=DAILY;INTERVAL=${pattern.interval}"
        is RecurrencePattern.Weekly -> {
            val byday = pattern.days
                .sortedBy { it.value }
                .joinToString(",") { dayToRfc(it) }
            "FREQ=WEEKLY;BYDAY=$byday"
        }
        is RecurrencePattern.Once -> {
            val dateTime = pattern.date.atTime(time)
            "RDATE=${dateTime.format(RDATE_FORMAT)}"
        }
        is RecurrencePattern.Custom -> pattern.rrule
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseByDay(byday: String): Set<DayOfWeek>? = runCatching {
        byday.split(",").map { rfcToDay(it.trim()) }.toSet()
    }.getOrNull()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun rfcToDay(rfc: String): DayOfWeek = when (rfc.uppercase()) {
        "MO" -> DayOfWeek.MONDAY
        "TU" -> DayOfWeek.TUESDAY
        "WE" -> DayOfWeek.WEDNESDAY
        "TH" -> DayOfWeek.THURSDAY
        "FR" -> DayOfWeek.FRIDAY
        "SA" -> DayOfWeek.SATURDAY
        "SU" -> DayOfWeek.SUNDAY
        else -> throw IllegalArgumentException("Unknown day: $rfc")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun dayToRfc(day: DayOfWeek): String = when (day) {
        DayOfWeek.MONDAY -> "MO"
        DayOfWeek.TUESDAY -> "TU"
        DayOfWeek.WEDNESDAY -> "WE"
        DayOfWeek.THURSDAY -> "TH"
        DayOfWeek.FRIDAY -> "FR"
        DayOfWeek.SATURDAY -> "SA"
        DayOfWeek.SUNDAY -> "SU"
    }
}