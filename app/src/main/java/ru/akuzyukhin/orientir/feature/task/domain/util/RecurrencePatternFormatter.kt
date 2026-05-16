package ru.akuzyukhin.orientir.feature.task.domain.util

import android.os.Build
import androidx.annotation.RequiresApi
import ru.akuzyukhin.orientir.feature.task.domain.model.RecurrencePattern
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
private val DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))

@RequiresApi(Build.VERSION_CODES.O)
fun RecurrencePattern.toHumanReadable(): String = when (this) {
    is RecurrencePattern.Daily -> "Каждый день"
    is RecurrencePattern.Weekly -> {
        val daysText = days
            .sortedBy { it.value }
            .joinToString(", ") { it.toShortRussian() }
        "По дням: $daysText"
    }
    is RecurrencePattern.EveryNDays -> "Каждые $interval ${dayWord(interval)}"
    is RecurrencePattern.Once -> "Однократно, ${date.format(DATE_FORMAT)}"
    is RecurrencePattern.Custom -> "Расширенное правило (только просмотр)"
}

@RequiresApi(Build.VERSION_CODES.O)
fun DayOfWeek.toShortRussian(): String = when (this) {
    DayOfWeek.MONDAY -> "ПН"
    DayOfWeek.TUESDAY -> "ВТ"
    DayOfWeek.WEDNESDAY -> "СР"
    DayOfWeek.THURSDAY -> "ЧТ"
    DayOfWeek.FRIDAY -> "ПТ"
    DayOfWeek.SATURDAY -> "СБ"
    DayOfWeek.SUNDAY -> "ВС"
}

@RequiresApi(Build.VERSION_CODES.O)
fun DayOfWeek.toFullRussian(): String =
    getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru")).replaceFirstChar { it.uppercase() }

private fun dayWord(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod100 in 11..14 -> "дней"
        mod10 == 1 -> "день"
        mod10 in 2..4 -> "дня"
        else -> "дней"
    }
}