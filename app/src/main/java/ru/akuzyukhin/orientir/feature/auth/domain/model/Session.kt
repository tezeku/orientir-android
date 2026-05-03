package ru.akuzyukhin.orientir.feature.auth.domain.model

/** Активная сессия пользователя - результат успешного логина/регистрации */
data class Session(
    val userId: Long,
    val role: Role
)
