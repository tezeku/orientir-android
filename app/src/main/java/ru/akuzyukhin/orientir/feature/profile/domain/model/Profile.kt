package ru.akuzyukhin.orientir.feature.profile.domain.model

import ru.akuzyukhin.orientir.feature.auth.domain.model.Role

/** Доменная модель профиля пользователя */
data class Profile(
    val id: Long,
    val surname: String,
    val name: String,
    val patronymic: String?,
    val phoneNumber: String,
    val role: Role,
    val isActive: Boolean,
    val email: String?,
    val address: String?
) {
    val fullName: String
        get() = listOfNotNull(surname, name, patronymic.takeIf { !it.isNullOrBlank() })
            .joinToString(" ")

    val shortName: String
        get() = if (patronymic.isNullOrBlank()) {
            name
        } else {
            "$name ${patronymic.first()}."
        }

}
