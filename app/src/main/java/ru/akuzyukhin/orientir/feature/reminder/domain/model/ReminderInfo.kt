package ru.akuzyukhin.orientir.feature.reminder.domain.model

import ru.akuzyukhin.orientir.feature.task.domain.model.Importance
import java.time.Instant

/** Информация о локальном напоминании о выполнении задачи */
data class ReminderInfo(
    val taskExecutionId: Long,
    val taskName: String,
    val importance: Importance,
    val scheduledAt: Instant,
    val windowMinutes: Int
)