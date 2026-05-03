package ru.akuzyukhin.orientir.core.data.network.dto

import kotlinx.serialization.Serializable

/** Стандартная структура ошибки от Spring Boot сервера */
@Serializable
data class ApiErrorDto(
    val timestamp: String? = null,
    val status: Int,
    val error: String? = null,
    val message: String? = null
)