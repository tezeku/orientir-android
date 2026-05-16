package ru.akuzyukhin.orientir.feature.task.domain.model

import java.time.LocalTime

/** Задача-шаблон в расписании */
data class Task(
    val id: Long,
    val scheduleId: Long,
    val name: String,
    val type: TaskType,
    val importance: Importance,
    val rrule: String,
    val scheduledTime: LocalTime,
    val windowMinutes: Int
)