package ru.akuzyukhin.orientir.feature.monitoring.domain.repository

import ru.akuzyukhin.orientir.feature.monitoring.domain.model.TaskExecution

/** Действия подопечного над экземплярами задач */
interface MonitoringRepository {

    /** Отметить выполнение задачи */
    suspend fun complete(taskExecutionId: Long): Result<TaskExecution>

    /** Осознанный пропуск (только для важности LOW) */
    suspend fun skip(taskExecutionId: Long): Result<TaskExecution>

    /** Сообщить о невозможности выполнения */
    suspend fun block(taskExecutionId: Long, comment: String? = null): Result<TaskExecution>
}