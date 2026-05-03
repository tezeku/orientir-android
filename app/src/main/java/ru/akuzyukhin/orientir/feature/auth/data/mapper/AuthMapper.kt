package ru.akuzyukhin.orientir.feature.auth.data.mapper

import ru.akuzyukhin.orientir.feature.auth.data.dto.AuthResponse
import ru.akuzyukhin.orientir.feature.auth.data.dto.RegisterRequest
import ru.akuzyukhin.orientir.feature.auth.domain.model.RegistrationData
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role
import ru.akuzyukhin.orientir.feature.auth.domain.model.Session

/** Преобразование между data-слоем (DTO) и domain-слоем (модели) */
fun AuthResponse.toSession(): Session {
    val role = Role.fromString(role)
        ?: throw IllegalStateException("Unknown role from server: ${this.role}")
    return Session(userId = userId, role = role)
}

/** Преобразование доменных данных регистрации в DTO для отправки на сервер */
fun RegistrationData.toRegisterRequest(): RegisterRequest = RegisterRequest(
    surname = surname,
    name = name,
    patronymic = patronymic,
    phoneNumber = phoneNumber,
    password = password,
    role = role.name,
    email = email,
    address = address
)
