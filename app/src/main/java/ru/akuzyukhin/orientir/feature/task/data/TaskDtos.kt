package ru.akuzyukhin.orientir.feature.task.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.akuzyukhin.orientir.feature.task.domain.model.ExecutionStatus
import ru.akuzyukhin.orientir.feature.task.domain.model.Importance
import ru.akuzyukhin.orientir.feature.task.domain.model.TaskType

/** DTO задачи-шаблона */
@Serializable
data class TaskDto(
    val id: Long,
    @SerialName("schedule_id")
    val scheduleId: Long,
    val name: String,
    val type: TaskType,
    val importance: Importance,
    val rrule: String,
    @SerialName("scheduled_time")
    val scheduledTime: String,
    @SerialName("window_minutes")
    val windowMinutes: Int
)

/** Запрос создания задачи */
@Serializable
data class CreateTaskRequestDto(
    val name: String,
    val type: TaskType,
    val importance: Importance,
    val rrule: String,
    @SerialName("scheduled_time")
    val scheduledTime: String,
    @SerialName("window_minutes")
    val windowMinutes: Int
)

/** Запрос частичного обновления задачи */
@Serializable
data class UpdateTaskRequestDto(
    val name: String? = null,
    val type: TaskType? = null,
    val importance: Importance? = null,
    val rrule: String? = null,
    @SerialName("scheduled_time")
    val scheduledTime: String? = null,
    @SerialName("window_minutes")
    val windowMinutes: Int? = null
)

/** Вложенная информация о задаче в DailyTask */
@Serializable
data class DailyTaskInfoDto(
    val id: Long,
    val name: String,
    val type: TaskType,
    val importance: Importance,
    @SerialName("window_minutes")
    val windowMinutes: Int
)

/** DTO daily-задачи */
@Serializable
data class DailyTaskDto(
    @SerialName("task_execution_id")
    val taskExecutionId: Long,
    val task: DailyTaskInfoDto,
    @SerialName("schedule_name")
    val scheduleName: String,
    @SerialName("scheduled_date_time")
    val scheduledDateTime: String,
    val status: ExecutionStatus,
    @SerialName("execution_time")
    val executionTime: String? = null,
    @SerialName("deviation_minutes")
    val deviationMinutes: Int? = null,
    @SerialName("is_within_window")
    val isWithinWindow: Boolean? = null
)