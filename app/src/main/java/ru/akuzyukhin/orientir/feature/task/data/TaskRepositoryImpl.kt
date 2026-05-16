package ru.akuzyukhin.orientir.feature.task.data

import android.os.Build
import androidx.annotation.RequiresApi
import ru.akuzyukhin.orientir.feature.task.domain.model.DailyTask
import ru.akuzyukhin.orientir.feature.task.domain.model.Importance
import ru.akuzyukhin.orientir.feature.task.domain.model.Task
import ru.akuzyukhin.orientir.feature.task.domain.model.TaskType
import ru.akuzyukhin.orientir.feature.task.domain.repository.TasksRepository
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class TasksRepositoryImpl @Inject constructor(
    private val api: TasksApi
) : TasksRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun createTask(
        wardId: Long,
        scheduleId: Long,
        name: String,
        type: TaskType,
        importance: Importance,
        rrule: String,
        scheduledTime: LocalTime,
        windowMinutes: Int
    ): Result<Task> = runCatching {
        api.createTask(
            wardId,
            scheduleId,
            CreateTaskRequestDto(
                name = name,
                type = type,
                importance = importance,
                rrule = rrule,
                scheduledTime = scheduledTime.toApiString(),
                windowMinutes = windowMinutes
            )
        ).toDomain()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getTasksBySchedule(wardId: Long, scheduleId: Long): Result<List<Task>> =
        runCatching { api.getTasksBySchedule(wardId, scheduleId).map { it.toDomain() } }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getTask(wardId: Long, scheduleId: Long, taskId: Long): Result<Task> =
        runCatching { api.getTask(wardId, scheduleId, taskId).toDomain() }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateTask(
        wardId: Long,
        scheduleId: Long,
        taskId: Long,
        name: String?,
        type: TaskType?,
        importance: Importance?,
        rrule: String?,
        scheduledTime: LocalTime?,
        windowMinutes: Int?
    ): Result<Task> = runCatching {
        api.updateTask(
            wardId, scheduleId, taskId,
            UpdateTaskRequestDto(
                name = name,
                type = type,
                importance = importance,
                rrule = rrule,
                scheduledTime = scheduledTime?.toApiString(),
                windowMinutes = windowMinutes
            )
        ).toDomain()
    }

    override suspend fun deleteTask(wardId: Long, scheduleId: Long, taskId: Long): Result<Unit> =
        runCatching { api.deleteTask(wardId, scheduleId, taskId) }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getDailyTasksForWard(wardId: Long, date: LocalDate): Result<List<DailyTask>> =
        runCatching { api.getDailyTasksForWard(wardId, date.toString()).map { it.toDomain() } }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getMyDailyTasks(date: LocalDate): Result<List<DailyTask>> =
        runCatching { api.getMyDailyTasks(date.toString()).map { it.toDomain() } }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getMyTask(scheduleId: Long, taskId: Long): Result<Task> =
        runCatching { api.getMyTask(scheduleId, taskId).toDomain() }
}
