package ru.akuzyukhin.orientir.feature.statistics.data.dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Ответ GET и PATCH /curators/me/wards/{id}/thresholds */
@Serializable
data class WardThresholdsDto(
    @SerialName("ward_id")
    val wardId: Long,
    @SerialName("min_completion_rate_percent")
    val minCompletionRatePercent: Int,
    @SerialName("max_overdue_rate_percent")
    val maxOverdueRatePercent: Int,
    @SerialName("max_avg_deviation_minutes")
    val maxAvgDeviationMinutes: Int,
    @SerialName("period_days")
    val periodDays: Int,
    @SerialName("max_global_deviation_percent")
    val maxGlobalDeviationPercent: Int
)

/** Тело PATCH /curators/me/wards/{id}/thresholds — все поля опциональны */
@Serializable
data class UpdateThresholdsRequestDto(
    @SerialName("min_completion_rate_percent")
    val minCompletionRatePercent: Int? = null,
    @SerialName("max_overdue_rate_percent")
    val maxOverdueRatePercent: Int? = null,
    @SerialName("max_avg_deviation_minutes")
    val maxAvgDeviationMinutes: Int? = null,
    @SerialName("period_days")
    val periodDays: Int? = null,
    @SerialName("max_global_deviation_percent")
    val maxGlobalDeviationPercent: Int? = null
)

/** Ответ GET /statistics/global-deviation */
@Serializable
data class GlobalDeviationDto(
    val wardId: Long,
    val period: PeriodDto,
    val globalDeviation: Double,
    val previousGlobalDeviation: Double,
    val threshold: Double,
    val isExceeded: Boolean,
    val trend: String
)

@Serializable
data class PeriodDto(
    val from: String,
    val to: String
)