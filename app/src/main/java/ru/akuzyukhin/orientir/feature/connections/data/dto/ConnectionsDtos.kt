package ru.akuzyukhin.orientir.feature.connections.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Краткие данные подопечного для списков */
@Serializable
data class WardSummaryDto(
    val id: Long,
    @SerialName("user_id")
    val userId: Long,
    val surname: String,
    val name: String,
    val patronymic: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String,
    val address: String? = null
)

/** Краткие данные куратора для списков */
@Serializable
data class CuratorSummaryDto(
    val id: Long,
    @SerialName("user_id")
    val userId: Long,
    val surname: String,
    val name: String,
    val patronymic: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String,
    val email: String
)

/** Тело запроса на привязку подопечного по номеру телефона */
@Serializable
data class AddWardRequestDto(
    @SerialName("phone_number")
    val wardPhoneNumber: String
)