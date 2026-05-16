package ru.akuzyukhin.orientir.feature.schedule.domain.repository

import ru.akuzyukhin.orientir.feature.schedule.domain.model.Schedule

/** Операция с расписаниями */
interface SchedulesRepository {
    suspend fun createScheduleForWard(wardId: Long, name: String): Result<Schedule>

    suspend fun getSchedulesByWard(wardId: Long): Result<List<Schedule>>

    suspend fun getScheduleByWard(wardId: Long, scheduleId: Long): Result<Schedule>

    suspend fun updateScheduleForWard(wardId: Long, scheduleId: Long, name: String): Result<Schedule>

    suspend fun deleteScheduleForWard(wardId: Long, scheduleId: Long): Result<Unit>

    suspend fun getMySchedules(): Result<List<Schedule>>

    suspend fun getMySchedule(scheduleId: Long): Result<Schedule>
}
