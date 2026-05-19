package ru.akuzyukhin.orientir.feature.schedule.ui.curator_list

import ru.akuzyukhin.orientir.core.ui.UiEvent
import ru.akuzyukhin.orientir.feature.connections.domain.model.WardSummary

data class CuratorScheduleListUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val wards: List<WardSummary> = emptyList()
)

sealed interface CuratorScheduleListUiEvent : UiEvent {
    data class NavigateToWardSchedules(val wardId: Long) : CuratorScheduleListUiEvent
}