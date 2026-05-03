package ru.akuzyukhin.orientir.core.data.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import ru.akuzyukhin.orientir.core.data.storage.TokenStorage
import javax.inject.Inject
import javax.inject.Singleton

/** OkHttp-интерсептор, добавляющий JWT access-токен в заголовок Authorization */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {
    private companion object {
        val PUBLIC_ENDPOINTS = listOf(
            "/auth/login",
            "/auth/register",
            "/auth/refresh"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val path = originalRequest.url.encodedPath
        if (PUBLIC_ENDPOINTS.any { path.endsWith(it) }) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { tokenStorage.getAccessToken() }
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}