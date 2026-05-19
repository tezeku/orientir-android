package ru.akuzyukhin.orientir.feature.statistics.ui.curator_list

import ru.akuzyukhin.orientir.core.ui.UiEvent
import ru.akuzyukhin.orientir.feature.statistics.domain.model.GlobalDeviation

data class CuratorStatisticsListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val items: List<WardStatisticsItem> = emptyList()
)

data class WardStatisticsItem(
    val wardId: Long,
    val displayName: String,
    val deviation: GlobalDeviation?
)

sealed interface CuratorStatisticsListUiEvent : UiEvent {
    data class NavigateToWardStatistics(val wardId: Long) : CuratorStatisticsListUiEvent
}