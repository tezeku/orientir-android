package ru.akuzyukhin.orientir.core.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Запрос на обновление токенов */
@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)

/** Ответ обновления токенов */
@Serializable
data class TokenPairResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val exiresIn: Long
)