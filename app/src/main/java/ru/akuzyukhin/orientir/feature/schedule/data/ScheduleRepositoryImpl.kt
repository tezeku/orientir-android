package ru.akuzyukhin.orientir.feature.schedule.data

import ru.akuzyukhin.orientir.feature.schedule.domain.model.Schedule
import ru.akuzyukhin.orientir.feature.schedule.domain.repository.SchedulesRepository
import javax.inject.Inject

/** Реализация репозитория расписаний поверх Retrofit-API */
class SchedulesRepositoryImpl @Inject constructor(
    private val api: SchedulesApi
) : SchedulesRepository {

    override suspend fun createScheduleForWard(wardId: Long, name: String): Result<Schedule> =
        runCatching { api.createScheduleForWard(wardId, CreateScheduleRequestDto(name)).toDomain() }

    override suspend fun getSchedulesByWard(wardId: Long): Result<List<Schedule>> =
        runCatching { api.getSchedulesByWard(wardId).map { it.toDomain() } }

    override suspend fun getScheduleByWard(wardId: Long, scheduleId: Long): Result<Schedule> =
        runCatching { api.getScheduleByWard(wardId, scheduleId).toDomain() }

    override suspend fun updateScheduleForWard(wardId: Long, scheduleId: Long, name: String): Result<Schedule> =
        runCatching {
            api.updateScheduleForWard(wardId, scheduleId, UpdateScheduleRequestDto(name)).toDomain()
        }

    override suspend fun deleteScheduleForWard(wardId: Long, scheduleId: Long): Result<Unit> =
        runCatching { api.deleteScheduleForWard(wardId, scheduleId) }

    override suspend fun getMySchedules(): Result<List<Schedule>> =
        runCatching { api.getMySchedules().map { it.toDomain() } }

    override suspend fun getMySchedule(scheduleId: Long): Result<Schedule> =
        runCatching { api.getMySchedule(scheduleId).toDomain() }
}