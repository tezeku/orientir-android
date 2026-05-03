package ru.akuzyukhin.orientir.feature.auth.domain.repository

import ru.akuzyukhin.orientir.feature.auth.domain.model.RegistrationData
import ru.akuzyukhin.orientir.feature.auth.domain.model.Session

/** Контракт работы с аутентификацией */
interface AuthRepository {
    /** Логин по телефону и паролю */
    suspend fun login(phoneNumber: String, password: String): Result<Session>

    /** Регисстрация нового пользователя */
    suspend fun register(data: RegistrationData): Result<Session>

    /** Выход из системы */
    suspend fun logout(): Result<Unit>
}