package ru.akuzyukhin.orientir.feature.auth.domain.repository

import retrofit2.HttpException
import ru.akuzyukhin.orientir.core.data.storage.TokenStorage
import ru.akuzyukhin.orientir.feature.auth.data.api.AuthApi
import ru.akuzyukhin.orientir.feature.auth.data.dto.LoginRequest
import ru.akuzyukhin.orientir.feature.auth.data.dto.LogoutRequest
import ru.akuzyukhin.orientir.feature.auth.data.mapper.toRegisterRequest
import ru.akuzyukhin.orientir.feature.auth.data.mapper.toSession
import ru.akuzyukhin.orientir.feature.auth.domain.model.RegistrationData
import ru.akuzyukhin.orientir.feature.auth.domain.model.Session
import javax.inject.Inject
import javax.inject.Singleton

/** Реализация [AuthRepository] поверх Retrofit + TokenStorage */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {
    override suspend fun login(phoneNumber: String, password: String): Result<Session> =
        runCatching {
            val response = authApi.login(LoginRequest(phoneNumber, password))
            tokenStorage.saveSession(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                userId = response.userId,
                role = response.role
            )
            response.toSession()
        }

    override suspend fun register(data: RegistrationData): Result<Session> =
        runCatching {
            val response = authApi.register(data.toRegisterRequest())
            tokenStorage.saveSession(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                userId = response.userId,
                role = response.role
            )
            response.toSession()
        }

    override suspend fun logout(): Result<Unit> = runCatching {
        try {
            val refreshToken = tokenStorage.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                authApi.logout(LogoutRequest(refreshToken))
            }
        } catch (e: HttpException) {
        } catch (e: Exception) {
        } finally {
            tokenStorage.clear()
        }
    }

}