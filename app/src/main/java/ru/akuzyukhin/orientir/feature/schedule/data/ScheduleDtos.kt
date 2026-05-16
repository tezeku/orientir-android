package ru.akuzyukhin.orientir.feature.schedule.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTO ответа с данными расписания */
@Serializable
data class ScheduleDto(
    val id: Long,
    val name: String,
    @SerialName("ward_id")
    val wardId: Long
)

/** DTO запроса на создание расписания */
@Serializable
data class CreateScheduleRequestDto(
    val name: String
)

/** DTO запроса на обновление расписания */
@Serializable
data class UpdateScheduleRequestDto(
    val name: String
)