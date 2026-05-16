package ru.akuzyukhin.orientir.feature.task.domain.repository

import ru.akuzyukhin.orientir.feature.task.domain.model.DailyTask
import ru.akuzyukhin.orientir.feature.task.domain.model.Importance
import ru.akuzyukhin.orientir.feature.task.domain.model.Task
import ru.akuzyukhin.orientir.feature.task.domain.model.TaskType
import java.time.LocalDate
import java.time.LocalTime

/** Операции с задачами */
interface TasksRepository {
    suspend fun createTask(
        wardId: Long,
        scheduleId: Long,
        name: String,
        type: TaskType,
        importance: Importance,
        rrule: String,
        scheduledTime: LocalTime,
        windowMinutes: Int
    ): Result<Task>

    suspend fun getTasksBySchedule(wardId: Long, scheduleId: Long): Result<List<Task>>

    suspend fun getTask(wardId: Long, scheduleId: Long, taskId: Long): Result<Task>

    suspend fun updateTask(
        wardId: Long,
        scheduleId: Long,
        taskId: Long,
        name: String? = null,
        type: TaskType? = null,
        importance: Importance? = null,
        rrule: String? = null,
        scheduledTime: LocalTime? = null,
        windowMinutes: Int? = null
    ): Result<Task>

    suspend fun deleteTask(wardId: Long, scheduleId: Long, taskId: Long): Result<Unit>

    suspend fun getDailyTasksForWard(wardId: Long, date: LocalDate): Result<List<DailyTask>>

    suspend fun getMyDailyTasks(date: LocalDate): Result<List<DailyTask>>

    suspend fun getMyTask(scheduleId: Long, taskId: Long): Result<Task>
}
