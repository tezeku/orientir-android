package ru.akuzyukhin.orientir.feature.monitoring.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.akuzyukhin.orientir.feature.task.domain.model.ExecutionStatus

/** DTO ответа с экземпляром задачи */
@Serializable
data class TaskExecutionDto(
    val id: Long,
    @SerialName("task_id")
    val taskId: Long,
    @SerialName("scheduled_date_time")
    val scheduledDateTime: String,
    @SerialName("execution_time")
    val executionTime: String? = null,
    val status: ExecutionStatus,
    @SerialName("deviation_minutes")
    val deviationMinutes: Int? = null,
    @SerialName("is_within_window")
    val isWithinWindow: Boolean? = null
)

/** DTO запроса блокировки задачи с опциональным комментарием */
@Serializable
data class BlockExecutionRequestDto(
    val comment: String? = null
)