package ru.akuzyukhin.orientir.feature.connections.ui.connections

import ru.akuzyukhin.orientir.core.ui.UiEvent
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role
import ru.akuzyukhin.orientir.feature.connections.domain.model.CuratorSummary
import ru.akuzyukhin.orientir.feature.connections.domain.model.WardSummary

/** Состояние экрана связей */
data class ConnectionsUiState(
    val role: Role? = null,

    val wards: List<WardSummary> = emptyList(),

    val curators: List<CuratorSummary> = emptyList(),

    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,

    val wardToRemove: WardSummary? = null,

    val isRemoving: Boolean = false
) {
    val isEmpty: Boolean
        get() = when (role) {
            Role.CURATOR -> wards.isEmpty()
            Role.WARD -> curators.isEmpty()
            null -> true
        }
}

sealed interface ConnectionsUiEvent : UiEvent {
    data object NavigateToAddWard : ConnectionsUiEvent

    data class NavigateToWardDetails(val wardId: Long) : ConnectionsUiEvent
}