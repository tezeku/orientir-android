package ru.akuzyukhin.orientir.feature.connections.data.repository

import ru.akuzyukhin.orientir.feature.connections.data.api.ConnectionsApi
import ru.akuzyukhin.orientir.feature.connections.data.dto.AddWardRequestDto
import ru.akuzyukhin.orientir.feature.connections.data.mapper.toDomain
import ru.akuzyukhin.orientir.feature.connections.domain.model.CuratorSummary
import ru.akuzyukhin.orientir.feature.connections.domain.model.WardSummary
import ru.akuzyukhin.orientir.feature.connections.domain.repository.ConnectionsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionsRepositoryImpl @Inject constructor(
    private val api: ConnectionsApi
) : ConnectionsRepository {

    override suspend fun getWards(): Result<List<WardSummary>> = runCatching {
        api.getWards().map { it.toDomain() }
    }

    override suspend fun getWard(wardId: Long): Result<WardSummary> = runCatching {
        api.getWard(wardId).toDomain()
    }

    override suspend fun addWard(wardPhoneNumber: String): Result<WardSummary> = runCatching {
        api.addWard(AddWardRequestDto(wardPhoneNumber = wardPhoneNumber)).toDomain()
    }

    override suspend fun removeWard(wardId: Long): Result<Unit> = runCatching {
        api.removeWard(wardId)
    }

    override suspend fun getCurators(): Result<List<CuratorSummary>> = runCatching {
        api.getCurators().map { it.toDomain() }
    }
}