package ru.akuzyukhin.orientir.feature.profile.domain.model

/** Доменная модель частичного обновления профиля */
data class ProfileUpdate(
    val surname: String? = null,
    val name: String? = null,
    val patronymic: String? = null,
    val email: String? = null,
    val address: String? = null
) {
    val hasChanges: Boolean
        get() = listOf(surname, name, patronymic, email, address).any { it != null }
}
