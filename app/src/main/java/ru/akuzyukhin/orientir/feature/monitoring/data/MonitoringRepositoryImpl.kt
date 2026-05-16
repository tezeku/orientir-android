package ru.akuzyukhin.orientir.feature.monitoring.data

import android.os.Build
import androidx.annotation.RequiresApi
import ru.akuzyukhin.orientir.feature.monitoring.domain.model.TaskExecution
import ru.akuzyukhin.orientir.feature.monitoring.domain.repository.MonitoringRepository
import javax.inject.Inject

class MonitoringRepositoryImpl @Inject constructor(
    private val api: MonitoringApi
) : MonitoringRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun complete(taskExecutionId: Long): Result<TaskExecution> =
        runCatching { api.complete(taskExecutionId).toDomain() }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun skip(taskExecutionId: Long): Result<TaskExecution> =
        runCatching { api.skip(taskExecutionId).toDomain() }

    override suspend fun block(taskExecutionId: Long, comment: String?): Result<TaskExecution> =
        runCatching {
            api.block(taskExecutionId, BlockExecutionRequestDto(comment = comment)).toDomain()
        }
}
