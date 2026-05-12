package ru.akuzyukhin.orientir.feature.connections.domain.model

/** Доменная модель краткой информации о кураторе */
data class CuratorSummary(
    val id: Long,
    val userId: Long,
    val surname: String,
    val name: String,
    val patronymic: String?,
    val phoneNumber: String,
    val email: String
) {
    val fullName: String
        get() = if (patronymic.isNullOrBlank()) {
            "$surname $name"
        } else {
            "$surname $name $patronymic"
        }

    val initials: String
        get() {
            val s = surname.firstOrNull()?.uppercase() ?: ""
            val n = name.firstOrNull()?.uppercase() ?: ""
            return "$s$n".ifBlank { "?" }
        }
}
