package ru.akuzyukhin.orientir.feature.task.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

/** Представление правила повторения задачи*/
sealed interface RecurrencePattern {

    /** Каждый день */
    data object Daily : RecurrencePattern

    /** По выбранным дням недели */
    data class Weekly(val days: Set<DayOfWeek>) : RecurrencePattern {
        init {
            require(days.isNotEmpty()) { "At least one day must be selected" }
        }
    }

    /** Каждый N-й день */
    data class EveryNDays(val interval: Int) : RecurrencePattern {
        init {
            require(interval >= 2) { "Interval must be at least 2 (use Daily for 1)" }
        }
    }

    /** Одноразовое событие в конкретную дату */
    data class Once(val date: LocalDate) : RecurrencePattern

    /** Сложное правило, не поддерживаемое UI-конструктором */
    data class Custom(val rrule: String) : RecurrencePattern
}