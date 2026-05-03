package ru.akuzyukhin.orientir.feature.auth.data.api

import retrofit2.http.Body
import retrofit2.http.POST
import ru.akuzyukhin.orientir.feature.auth.data.dto.AuthResponse
import ru.akuzyukhin.orientir.feature.auth.data.dto.LoginRequest
import ru.akuzyukhin.orientir.feature.auth.data.dto.LogoutRequest
import ru.akuzyukhin.orientir.feature.auth.data.dto.RegisterRequest

/** Retrofit-интерфейс */
interface AuthApi {
    /** Вход по номеру телефона и паролю */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    /** Регистрация нового пользователя */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    /** Выход из системы - инвалидация refresh-токена на сервере */
    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest)
}