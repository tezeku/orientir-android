package ru.akuzyukhin.orientir.core.data.network

import kotlinx.serialization.json.Json
import retrofit2.HttpException
import ru.akuzyukhin.orientir.core.data.network.dto.ApiErrorDto

private val errorJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

fun HttpException.serverMessage(): String? {
    return try {
        val rawBody = response()?.errorBody()?.string() ?: return null
        if (rawBody.isBlank()) return null
        val apiError = errorJson.decodeFromString(ApiErrorDto.serializer(), rawBody)
        apiError.message?.takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        null
    }
}
