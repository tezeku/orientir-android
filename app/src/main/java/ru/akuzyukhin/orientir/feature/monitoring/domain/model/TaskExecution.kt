package ru.akuzyukhin.orientir.feature.monitoring.domain.model

import ru.akuzyukhin.orientir.feature.task.domain.model.ExecutionStatus
import java.time.LocalDateTime

/** Состояние экземпляра задачи после действия мониторинга */
data class TaskExecution(
    val id: Long,
    val taskId: Long,
    val scheduledDateTime: LocalDateTime,
    val executionTime: LocalDateTime?,
    val status: ExecutionStatus,
    val deviationMinutes: Int?,
    val isWithinWindow: Boolean?
)
