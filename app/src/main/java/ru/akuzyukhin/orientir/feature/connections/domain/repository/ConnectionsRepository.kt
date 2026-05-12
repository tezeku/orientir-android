package ru.akuzyukhin.orientir.feature.connections.domain.repository

import ru.akuzyukhin.orientir.feature.connections.domain.model.CuratorSummary
import ru.akuzyukhin.orientir.feature.connections.domain.model.WardSummary

/** Работа со связями куратор-подопечный */
interface ConnectionsRepository {

    suspend fun getWards(): Result<List<WardSummary>>

    suspend fun getWard(wardId: Long): Result<WardSummary>

    suspend fun addWard(wardPhoneNumber: String): Result<WardSummary>

    suspend fun removeWard(wardId: Long): Result<Unit>

    suspend fun getCurators(): Result<List<CuratorSummary>>
}
