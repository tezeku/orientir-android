package ru.akuzyukhin.orientir.feature.task.domain.util

import android.os.Build
import androidx.annotation.RequiresApi
import ru.akuzyukhin.orientir.feature.task.domain.model.RecurrencePattern
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
                val knownKeys = setOf("FREQ", "INTERVAL", "DTSTART")
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
                val knownKeys = setOf("FREQ", "BYDAY", "INTERVAL", "DTSTART")
                if (parts.keys.any { it !in knownKeys }) {
                    return RecurrencePattern.Custom(rrule)
                }

                RecurrencePattern.Weekly(days)
            }

            else -> RecurrencePattern.Custom(rrule)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun build(pattern: RecurrencePattern, time: LocalTime, startDate: LocalDate? = null): String = when (pattern) {
        is RecurrencePattern.Daily -> "FREQ=DAILY"
        is RecurrencePattern.EveryNDays -> {
            val base = "FREQ=DAILY;INTERVAL=${pattern.interval}"
            if (startDate != null) "$base;DTSTART=${startDate.atTime(time).format(RDATE_FORMAT)}"
            else base
        }
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

    /** Извлекает дату DTSTART из rrule-строки, если она присутствует */
    @RequiresApi(Build.VERSION_CODES.O)
    fun parseDtstart(rrule: String): LocalDate? {
        val value = rrule.split(";")
            .firstOrNull { it.startsWith("DTSTART=") }
            ?.removePrefix("DTSTART=") ?: return null
        return runCatching {
            LocalDate.parse(value.take(8), DateTimeFormatter.ofPattern("yyyyMMdd"))
        }.getOrNull()
    }

    /**
     * Проверяет, является ли [date] допустимым днём для задачи с данным [rrule].
     * Возвращает true, если проверить невозможно (нет якорной даты).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isDayValid(rrule: String, date: LocalDate): Boolean {
        if (rrule.isBlank()) return true
        val trimmed = rrule.trim()

        if (trimmed.startsWith("RDATE=")) {
            val value = trimmed.removePrefix("RDATE=")
            val occurrenceDate = runCatching {
                LocalDate.parse(value.take(8), DateTimeFormatter.ofPattern("yyyyMMdd"))
            }.getOrElse { return true }
            return date == occurrenceDate
        }

        val parts: Map<String, String> = trimmed.split(";")
            .mapNotNull {
                val eq = it.indexOf('=')
                if (eq < 0) null else it.substring(0, eq).trim() to it.substring(eq + 1).trim()
            }.toMap()

        return when (parts["FREQ"]) {
            "DAILY" -> {
                val interval = parts["INTERVAL"]?.toIntOrNull() ?: 1
                if (interval <= 1) return true
                val dtstart = parseDtstart(trimmed) ?: return true
                val days = ChronoUnit.DAYS.between(dtstart, date)
                days >= 0 && days % interval == 0L
            }
            "WEEKLY" -> {
                val byday = parts["BYDAY"] ?: return true
                val validDays = parseByDay(byday) ?: return true
                date.dayOfWeek in validDays
            }
            else -> true
        }
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