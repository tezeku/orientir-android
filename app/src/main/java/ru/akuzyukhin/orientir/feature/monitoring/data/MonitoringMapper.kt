package ru.akuzyukhin.orientir.feature.monitoring.data

import android.os.Build
import androidx.annotation.RequiresApi
import ru.akuzyukhin.orientir.feature.monitoring.domain.model.TaskExecution
import java.time.LocalDateTime

/** DTO в Domain. */
@RequiresApi(Build.VERSION_CODES.O)
internal fun TaskExecutionDto.toDomain() = TaskExecution(
    id = id,
    taskId = taskId,
    scheduledDateTime = LocalDateTime.parse(scheduledDateTime),
    executionTime = executionTime?.let { LocalDateTime.parse(it) },
    status = status,
    deviationMinutes = deviationMinutes,
    isWithinWindow = isWithinWindow
)