package ru.akuzyukhin.orientir.feature.schedule.domain.model

/** Расписание подопечного - контейнер для задач */
data class Schedule(
    val id: Long,
    val name: String,
    val wardId: Long
)