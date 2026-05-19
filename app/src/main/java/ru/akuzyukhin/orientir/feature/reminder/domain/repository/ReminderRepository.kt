package ru.akuzyukhin.orientir.feature.reminder.domain.repository

import ru.akuzyukhin.orientir.feature.reminder.domain.model.ReminderInfo

/** Контракт планироващика локальных напоминаний о задачах */
interface ReminderRepository {

    suspend fun schedule(reminder: ReminderInfo)

    suspend fun cancel(taskExecutionId: Long)

    suspend fun sync(reminders: List<ReminderInfo>)

    suspend fun restoreFromStorage()
}