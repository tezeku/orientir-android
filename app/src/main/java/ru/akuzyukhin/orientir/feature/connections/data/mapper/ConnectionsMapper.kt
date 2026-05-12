package ru.akuzyukhin.orientir.feature.connections.data.mapper

import ru.akuzyukhin.orientir.feature.connections.data.dto.CuratorSummaryDto
import ru.akuzyukhin.orientir.feature.connections.data.dto.WardSummaryDto
import ru.akuzyukhin.orientir.feature.connections.domain.model.CuratorSummary
import ru.akuzyukhin.orientir.feature.connections.domain.model.WardSummary

fun WardSummaryDto.toDomain(): WardSummary = WardSummary(
    id = id,
    userId = userId,
    surname = surname,
    name = name,
    patronymic = patronymic,
    phoneNumber = phoneNumber,
    address = address
)

fun CuratorSummaryDto.toDomain(): CuratorSummary = CuratorSummary(
    id = id,
    userId = userId,
    surname = surname,
    name = name,
    patronymic = patronymic,
    phoneNumber = phoneNumber,
    email = email
)
