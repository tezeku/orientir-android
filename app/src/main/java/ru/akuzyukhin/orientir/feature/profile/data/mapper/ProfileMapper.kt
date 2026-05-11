package ru.akuzyukhin.orientir.feature.profile.data.mapper

import ru.akuzyukhin.orientir.feature.auth.domain.model.Role
import ru.akuzyukhin.orientir.feature.profile.data.dto.ProfileResponseDto
import ru.akuzyukhin.orientir.feature.profile.data.dto.UpdateProfileRequestDto
import ru.akuzyukhin.orientir.feature.profile.domain.model.Profile
import ru.akuzyukhin.orientir.feature.profile.domain.model.ProfileUpdate

/** Маппинг DTO профиля от сервера в доменную модель */
fun ProfileResponseDto.toDomain(): Profile {
    val role = Role.fromString(this.role)
        ?: throw IllegalStateException("Unknown role from server: ${this.role}")

    return Profile(
        id = id,
        surname = surname,
        name = name,
        patronymic = patronymic,
        phoneNumber = phoneNumber,
        role = role,
        isActive = isActive,
        email = curatorProfile?.email,
        address = wardProfile?.address
    )
}

/** Маппинг доменного ProfileUpdate в DTO для PATCH-запроса */
fun ProfileUpdate.toRequestDto(): UpdateProfileRequestDto = UpdateProfileRequestDto(
    surname = surname,
    name = name,
    patronymic = patronymic,
    email = email,
    address = address
)