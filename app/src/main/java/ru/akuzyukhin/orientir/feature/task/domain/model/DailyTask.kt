package ru.akuzyukhin.orientir.feature.task.domain.model

import java.time.LocalDateTime

/** Экземпляр задачи на конкретный момент времени */
data class DailyTask(
    val taskExecutionId: Long,
    val taskId: Long,
    val taskName: String,
    val taskType: TaskType,
    val importance: Importance,
    val windowMinutes: Int,
    val scheduleName: String,
    val scheduledDateTime: LocalDateTime,
    val status: ExecutionStatus,
    val executionTime: LocalDateTime?,
    val deviationMinutes: Int?,
    val isWithinWindow: Boolean?
)