package ru.akuzyukhin.orientir.feature.schedule.data

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Retrofit-интерфейс расписаний */
interface SchedulesApi {
    @POST("curators/me/wards/{wardId}/schedules")
    suspend fun createScheduleForWard(
        @Path("wardId") wardId: Long,
        @Body request: CreateScheduleRequestDto
    ): ScheduleDto

    @GET("curators/me/wards/{wardId}/schedules")
    suspend fun getSchedulesByWard(@Path("wardId") wardId: Long): List<ScheduleDto>

    @GET("curators/me/wards/{wardId}/schedules/{scheduleId}")
    suspend fun getScheduleByWard(
        @Path("wardId") wardId: Long,
        @Path("scheduleId") scheduleId: Long
    ): ScheduleDto

    @PATCH("curators/me/wards/{wardId}/schedules/{scheduleId}")
    suspend fun updateScheduleForWard(
        @Path("wardId") wardId: Long,
        @Path("scheduleId") scheduleId: Long,
        @Body request: UpdateScheduleRequestDto
    ): ScheduleDto

    @DELETE("curators/me/wards/{wardId}/schedules/{scheduleId}")
    suspend fun deleteScheduleForWard(
        @Path("wardId") wardId: Long,
        @Path("scheduleId") scheduleId: Long
    )

    @GET("wards/me/schedules")
    suspend fun getMySchedules(): List<ScheduleDto>

    @GET("wards/me/schedules/{scheduleId}")
    suspend fun getMySchedule(@Path("scheduleId") scheduleId: Long): ScheduleDto
}