package ru.akuzyukhin.orientir.core.data.network

import android.util.Log
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import ru.akuzyukhin.orientir.BuildConfig
import ru.akuzyukhin.orientir.core.data.network.dto.RefreshTokenRequest
import ru.akuzyukhin.orientir.core.data.network.dto.TokenPairResponse
import ru.akuzyukhin.orientir.core.data.storage.TokenStorage
import javax.inject.Inject
import javax.inject.Singleton

/** OkHttp Authenticator - реагирует на ответы 401 Unauthorized */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val json: Json
) : Authenticator {
    private val refreshMutex = Mutex()
    private val refreshClient by lazy { OkHttpClient.Builder().build() }

    override fun authenticate(route: Route?, response: Response): Request? {
        val currentRefreshToken = runBlocking { tokenStorage.getRefreshToken() }
            ?: return null

        val tokenInFailedRequest = response.request.header("Authorization")
            ?.removePrefix("Bearer ")

        return runBlocking {
            refreshMutex.withLock {
                val currentAccessToken = tokenStorage.getAccessToken()

                if (currentAccessToken != null && currentAccessToken != tokenInFailedRequest) {
                    return@withLock buildRequestWithNewToken(response.request, currentAccessToken)
                }

                val newTokens = tryRefresh(currentRefreshToken)
                if (newTokens != null) {
                    tokenStorage.updateTokens(newTokens.accessToken, newTokens.refreshToken)
                    buildRequestWithNewToken(response.request, newTokens.accessToken)
                } else {
                    tokenStorage.clear()
                    null
                }
            }
        }
    }

    private fun tryRefresh(refreshToken: String): TokenPairResponse? {
        return try {
            val requestBody = json.encodeToString(
                RefreshTokenRequest.serializer(),
                RefreshTokenRequest(refreshToken)
            ).toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${BuildConfig.BASE_URL}auth/refresh")
                .post(requestBody)
                .build()

            refreshClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                json.decodeFromString(TokenPairResponse.serializer(), body)
            }
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "Failed to refresh token", e)
            null
        }
    }

    private fun buildRequestWithNewToken(originalRequest: Request, newToken: String): Request {
        return originalRequest.newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", "Bearer $newToken")
            .build()
    }

}