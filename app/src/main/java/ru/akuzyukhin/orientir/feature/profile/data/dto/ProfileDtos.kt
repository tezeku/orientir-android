package ru.akuzyukhin.orientir.feature.profile.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Ответ GET /users/me и PATCH /users/me */
@Serializable
data class ProfileResponseDto(
    val id: Long,
    val surname: String,
    val name: String,
    val patronymic: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String,
    val role: String,
    @SerialName("is_active")
    val isActive: Boolean,
    @SerialName("curator_profile")
    val curatorProfile: CuratorProfileDto? = null,
    @SerialName("ward_profile")
    val wardProfile: WardProfileDto? = null
)

/** Данные профиля куратора */
@Serializable
data class CuratorProfileDto(
    val id: Long,
    val email: String
)

/** Данные профиля подопечного */
@Serializable
data class WardProfileDto(
    val id: Long,
    val address: String? = null
)

/** Тело PATCH /users/me - частичное обновление */
@Serializable
data class UpdateProfileRequestDto(
    val surname: String? = null,
    val name: String? = null,
    val patronymic: String? = null,
    val email: String? = null,
    val address: String? = null
)

/** Тело POST /users/me/password */
@Serializable
data class ChangePasswordRequestDto(
    @SerialName("current_password")
    val currentPassword: String,
    @SerialName("new_password")
    val newPassword: String
)