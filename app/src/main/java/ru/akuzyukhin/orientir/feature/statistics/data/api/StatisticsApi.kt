package ru.akuzyukhin.orientir.feature.statistics.data.api


import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query
import ru.akuzyukhin.orientir.feature.statistics.data.dto.GlobalDeviationDto
import ru.akuzyukhin.orientir.feature.statistics.data.dto.UpdateThresholdsRequestDto
import ru.akuzyukhin.orientir.feature.statistics.data.dto.WardThresholdsDto

/** Retrofit-интерфейс эндпоинтов статистики и порогов */
interface StatisticsApi {

    @GET("curators/me/wards/{wardId}/thresholds")
    suspend fun getThresholds(@Path("wardId") wardId: Long): WardThresholdsDto

    @PATCH("curators/me/wards/{wardId}/thresholds")
    suspend fun updateThresholds(
        @Path("wardId") wardId: Long,
        @Body request: UpdateThresholdsRequestDto
    ): WardThresholdsDto

    @GET("curators/me/wards/{wardId}/statistics/global-deviation")
    suspend fun getGlobalDeviation(
        @Path("wardId") wardId: Long,
        @Query("from") from: String,
        @Query("to") to: String
    ): GlobalDeviationDto
}