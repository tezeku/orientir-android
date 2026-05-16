package ru.akuzyukhin.orientir.feature.task.ui.statistics

import ru.akuzyukhin.orientir.feature.task.domain.model.ExecutionStatus

enum class StatisticsPeriod(val days: Int, val label: String) {
    WEEK(7, "Неделя"),
    MONTH(30, "Месяц")
}

data class StatisticsCounters(
    val total: Int = 0,
    val byStatus: Map<ExecutionStatus, Int> = emptyMap()
) {
    val completedTotal: Int
        get() = (byStatus[ExecutionStatus.COMPLETED] ?: 0) +
                (byStatus[ExecutionStatus.COMPLETED_LATE] ?: 0)

    val completionRate: Float
        get() = if (total == 0) 0f else completedTotal.toFloat() / total
}

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val period: StatisticsPeriod = StatisticsPeriod.WEEK,
    val counters: StatisticsCounters = StatisticsCounters(),
    val errorMessage: String? = null
)