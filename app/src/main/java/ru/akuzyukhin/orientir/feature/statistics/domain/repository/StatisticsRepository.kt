package ru.akuzyukhin.orientir.feature.statistics.domain.repository

import ru.akuzyukhin.orientir.feature.statistics.domain.model.GlobalDeviation
import ru.akuzyukhin.orientir.feature.statistics.domain.model.WardThresholds
import java.time.LocalDate

/** Работа с настраиваемыми порогами и статистикой состояния подопечного */
interface StatisticsRepository {

    suspend fun getThresholds(wardId: Long): Result<WardThresholds>

    suspend fun updateThresholds(
        wardId: Long,
        minCompletionRatePercent: Int? = null,
        maxOverdueRatePercent: Int? = null,
        maxAvgDeviationMinutes: Int? = null,
        periodDays: Int? = null,
        maxGlobalDeviationPercent: Int? = null
    ): Result<WardThresholds>

    suspend fun getGlobalDeviation(
        wardId: Long,
        from: LocalDate,
        to: LocalDate
    ): Result<GlobalDeviation>
}