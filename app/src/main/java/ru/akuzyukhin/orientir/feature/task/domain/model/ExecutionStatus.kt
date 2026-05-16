package ru.akuzyukhin.orientir.feature.task.domain.model

/** Статус выполнения экземпляра задачи */
enum class ExecutionStatus {
    PENDING,
    COMPLETED,
    COMPLETED_LATE,
    SKIPPED,
    OVERDUE,
    BLOCKED
}
