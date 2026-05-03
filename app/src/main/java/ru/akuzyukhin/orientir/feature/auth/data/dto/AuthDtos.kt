package ru.akuzyukhin.orientir.feature.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Запрос на вход в систему */
@Serializable
data class LoginRequest(
    @SerialName("phone_number")
    val phoneNumber: String,
    val password: String
)

/** Запрос на регистрацию нового пользователя */
@Serializable
data class RegisterRequest(
    val surname: String,
    val name: String,
    val patronymic: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String,
    val password: String,
    val role: String,
    val email: String? = null,
    val address: String? = null
)

/**
 * Запрос на выход из системы. Содержит refresh-токен,
 * который сервер инвалидирует в БД.
 */
@Serializable
data class LogoutRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)

/**
 * Ответ /auth/login и /auth/register. Включает пару токенов и
 * базовую информацию о пользователе для сохранения в TokenStorage.
 */
@Serializable
data class AuthResponse(
    @SerialName("user_id")
    val userId: Long,
    val role: String,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: Long
)