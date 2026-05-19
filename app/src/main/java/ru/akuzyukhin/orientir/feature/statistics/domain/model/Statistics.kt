package ru.akuzyukhin.orientir.feature.statistics.domain.model

import java.time.LocalDate

/** Настраиваемые куратором пороги нарушений для подопечного */
data class WardThresholds(
    val wardId: Long,
    val minCompletionRatePercent: Int,
    val maxOverdueRatePercent: Int,
    val maxAvgDeviationMinutes: Int,
    val periodDays: Int,
    val maxGlobalDeviationPercent: Int
)

/** Глобальный коэффициент отклонений за период с трендом */
data class GlobalDeviation(
    val wardId: Long,
    val periodFrom: LocalDate,
    val periodTo: LocalDate,
    val current: Double,
    val previous: Double,
    val threshold: Double,
    val isExceeded: Boolean,
    val trend: Trend
)

enum class Trend {
    IMPROVING,
    WORSENING,
    STABLE,
    UNKNOWN
}