package ru.akuzyukhin.orientir.feature.connections.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.akuzyukhin.orientir.feature.connections.data.dto.AddWardRequestDto
import ru.akuzyukhin.orientir.feature.connections.data.dto.CuratorSummaryDto
import ru.akuzyukhin.orientir.feature.connections.data.dto.WardSummaryDto

/** Retrofit-интерфейс эндпоинтов связей */
interface ConnectionsApi {
    @GET("curators/me/wards")
    suspend fun getWards(): List<WardSummaryDto>

    @GET("curators/me/wards/{wardId}")
    suspend fun getWard(@Path("wardId") wardId: Long): WardSummaryDto

    @POST("curators/me/wards")
    suspend fun addWard(@Body request: AddWardRequestDto): WardSummaryDto

    @DELETE("curators/me/wards/{wardId}")
    suspend fun removeWard(@Path("wardId") wardId: Long)

    @GET("wards/me/curators")
    suspend fun getCurators(): List<CuratorSummaryDto>
}