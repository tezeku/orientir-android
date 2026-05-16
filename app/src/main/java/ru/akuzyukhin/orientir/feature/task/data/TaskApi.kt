package ru.akuzyukhin.orientir.feature.task.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit-интерфейс задач */
interface TasksApi {
    @POST("curators/me/wards/{wardId}/schedules/{scheduleId}/tasks")
    suspend fun createTask(
        @Path("wardId") wardId: Long,
        @Path("scheduleId") scheduleId: Long,
        @Body request: CreateTaskRequestDto
    ): TaskDto

    @GET("curators/me/wards/{wardId}/schedules/{scheduleId}/tasks")
    suspend fun getTasksBySchedule(
        @Path("wardId") wardId: Long,
        @Path("scheduleId") scheduleId: Long
    ): List<TaskDto>

    @GET("curators/me/wards/{wardId}/schedules/{scheduleId}/tasks/{taskId}")
    suspend fun getTask(
        @Path("wardId") wardId: Long,
        @Path("scheduleId") scheduleId: Long,
        @Path("taskId") taskId: Long
    ): TaskDto

    @PATCH("curators/me/wards/{wardId}/schedules/{scheduleId}/tasks/{taskId}")
    suspend fun updateTask(
        @Path("wardId") wardId: Long,
        @Path("scheduleId") scheduleId: Long,
        @Path("taskId") taskId: Long,
        @Body request: UpdateTaskRequestDto
    ): TaskDto

    @DELETE("curators/me/wards/{wardId}/schedules/{scheduleId}/tasks/{taskId}")
    suspend fun deleteTask(
        @Path("wardId") wardId: Long,
        @Path("scheduleId") scheduleId: Long,
        @Path("taskId") taskId: Long
    )

    @GET("curators/me/wards/{wardId}/tasks/daily")
    suspend fun getDailyTasksForWard(
        @Path("wardId") wardId: Long,
        @Query("date") date: String
    ): List<DailyTaskDto>

    @GET("wards/me/tasks/daily")
    suspend fun getMyDailyTasks(@Query("date") date: String): List<DailyTaskDto>

    @GET("wards/me/schedules/{scheduleId}/tasks/{taskId}")
    suspend fun getMyTask(
        @Path("scheduleId") scheduleId: Long,
        @Path("taskId") taskId: Long
    ): TaskDto
}