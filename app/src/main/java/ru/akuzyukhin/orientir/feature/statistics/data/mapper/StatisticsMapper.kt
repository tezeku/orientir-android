package ru.akuzyukhin.orientir.feature.statistics.data.mapper

import ru.akuzyukhin.orientir.feature.statistics.data.dto.GlobalDeviationDto
import ru.akuzyukhin.orientir.feature.statistics.data.dto.WardThresholdsDto
import ru.akuzyukhin.orientir.feature.statistics.domain.model.GlobalDeviation
import ru.akuzyukhin.orientir.feature.statistics.domain.model.Trend
import ru.akuzyukhin.orientir.feature.statistics.domain.model.WardThresholds
import java.time.LocalDate

fun WardThresholdsDto.toDomain(): WardThresholds = WardThresholds(
    wardId = wardId,
    minCompletionRatePercent = minCompletionRatePercent,
    maxOverdueRatePercent = maxOverdueRatePercent,
    maxAvgDeviationMinutes = maxAvgDeviationMinutes,
    periodDays = periodDays,
    maxGlobalDeviationPercent = maxGlobalDeviationPercent
)

fun GlobalDeviationDto.toDomain(): GlobalDeviation = GlobalDeviation(
    wardId = wardId,
    periodFrom = LocalDate.parse(period.from),
    periodTo = LocalDate.parse(period.to),
    current = globalDeviation,
    previous = previousGlobalDeviation,
    threshold = threshold,
    isExceeded = isExceeded,
    trend = runCatching { Trend.valueOf(trend) }.getOrDefault(Trend.UNKNOWN)
)