package ru.akuzyukhin.orientir.feature.task.data

import android.os.Build
import androidx.annotation.RequiresApi
import ru.akuzyukhin.orientir.feature.task.domain.model.DailyTask
import ru.akuzyukhin.orientir.feature.task.domain.model.Task
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

/** DTO в Domain */
@RequiresApi(Build.VERSION_CODES.O)
internal fun TaskDto.toDomain() = Task(
    id = id,
    scheduleId = scheduleId,
    name = name,
    type = type,
    importance = importance,
    rrule = rrule,
    scheduledTime = LocalTime.parse(scheduledTime, TIME_FORMATTER),
    windowMinutes = windowMinutes
)

/** DTO в Domain */
@RequiresApi(Build.VERSION_CODES.O)
internal fun DailyTaskDto.toDomain() = DailyTask(
    taskExecutionId = taskExecutionId,
    taskId = task.id,
    taskName = task.name,
    taskType = task.type,
    importance = task.importance,
    windowMinutes = task.windowMinutes,
    scheduleName = scheduleName,
    scheduledDateTime = LocalDateTime.parse(scheduledDateTime),
    status = status,
    executionTime = executionTime?.let { LocalDateTime.parse(it) },
    deviationMinutes = deviationMinutes,
    isWithinWindow = isWithinWindow,
    rrule = task.rrule
)

/** LocalTime в "HH:mm:ss" для отправки на сервер */
@RequiresApi(Build.VERSION_CODES.O)
internal fun LocalTime.toApiString(): String = format(TIME_FORMATTER)

/** LocalDateTime в ISO-строка для сравнений */
internal fun LocalDateTime.toApiString(): String = toString()