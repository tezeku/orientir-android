package ru.akuzyukhin.orientir.feature.statistics.domain.repository

import ru.akuzyukhin.orientir.feature.statistics.data.api.StatisticsApi
import ru.akuzyukhin.orientir.feature.statistics.data.dto.UpdateThresholdsRequestDto
import ru.akuzyukhin.orientir.feature.statistics.data.mapper.toDomain
import ru.akuzyukhin.orientir.feature.statistics.domain.model.GlobalDeviation
import ru.akuzyukhin.orientir.feature.statistics.domain.model.WardThresholds
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val api: StatisticsApi
) : StatisticsRepository {

    override suspend fun getThresholds(wardId: Long): Result<WardThresholds> =
        runCatching { api.getThresholds(wardId).toDomain() }

    override suspend fun updateThresholds(
        wardId: Long,
        minCompletionRatePercent: Int?,
        maxOverdueRatePercent: Int?,
        maxAvgDeviationMinutes: Int?,
        periodDays: Int?,
        maxGlobalDeviationPercent: Int?
    ): Result<WardThresholds> = runCatching {
        api.updateThresholds(
            wardId = wardId,
            request = UpdateThresholdsRequestDto(
                minCompletionRatePercent = minCompletionRatePercent,
                maxOverdueRatePercent = maxOverdueRatePercent,
                maxAvgDeviationMinutes = maxAvgDeviationMinutes,
                periodDays = periodDays,
                maxGlobalDeviationPercent = maxGlobalDeviationPercent
            )
        ).toDomain()
    }

    override suspend fun getGlobalDeviation(
        wardId: Long,
        from: LocalDate,
        to: LocalDate
    ): Result<GlobalDeviation> = runCatching {
        api.getGlobalDeviation(
            wardId = wardId,
            from = from.toString(),
            to = to.toString()
        ).toDomain()
    }
}