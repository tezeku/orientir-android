package ru.akuzyukhin.orientir.feature.connections.ui.ward_detail

import ru.akuzyukhin.orientir.feature.connections.domain.model.WardSummary

data class WardDetailUiState(
    val isLoading: Boolean = true,
    val ward: WardSummary? = null,
    val isRemoving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface WardDetailEvent {
    data object WardRemoved : WardDetailEvent
    data class ShowError(val message: String) : WardDetailEvent
}
