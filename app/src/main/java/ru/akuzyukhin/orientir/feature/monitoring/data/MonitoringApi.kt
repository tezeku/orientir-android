package ru.akuzyukhin.orientir.feature.monitoring.data

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/** Retrofit-интерфейс мониторинга для подопечного */
interface MonitoringApi {

    @POST("wards/me/task-executions/{taskExecutionId}/complete")
    suspend fun complete(@Path("taskExecutionId") taskExecutionId: Long): TaskExecutionDto

    @POST("wards/me/task-executions/{taskExecutionId}/skip")
    suspend fun skip(@Path("taskExecutionId") taskExecutionId: Long): TaskExecutionDto

    @POST("wards/me/task-executions/{taskExecutionId}/block")
    suspend fun block(
        @Path("taskExecutionId") taskExecutionId: Long,
        @Body request: BlockExecutionRequestDto
    ): TaskExecutionDto
}
