package ru.akuzyukhin.orientir.feature.schedule.data

import ru.akuzyukhin.orientir.feature.schedule.domain.model.Schedule

/** DTO в Domain */
internal fun ScheduleDto.toDomain() = Schedule(
    id = id,
    name = name,
    wardId = wardId
)