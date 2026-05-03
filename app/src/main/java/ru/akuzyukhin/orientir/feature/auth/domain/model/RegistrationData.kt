package ru.akuzyukhin.orientir.feature.auth.domain.model

/** Данные для регистрации нового пользователя */
data class RegistrationData(
    val surname: String,
    val name: String,
    val patronymic: String?,
    val phoneNumber: String,
    val password: String,
    val role: Role,
    val email: String?,
    val address: String?
)
