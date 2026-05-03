package ru.akuzyukhin.orientir.feature.auth.domain.model

/** Роль пользователя в системе */
enum class Role {
    CURATOR,
    WARD;

    companion object {
        fun fromString(value: String?): Role? = when (value?.uppercase()) {
            "CURATOR" -> CURATOR
            "WARD" -> WARD
            else -> null
        }
    }
}