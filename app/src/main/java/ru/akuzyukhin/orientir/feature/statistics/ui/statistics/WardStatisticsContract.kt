package ru.akuzyukhin.orientir.feature.statistics.ui.statistics

import ru.akuzyukhin.orientir.core.ui.UiEvent
import ru.akuzyukhin.orientir.feature.statistics.domain.model.GlobalDeviation

data class WardStatisticsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val deviation: GlobalDeviation? = null
)

sealed interface WardStatisticsUiEvent : UiEvent {
    data object NavigateBack : WardStatisticsUiEvent
    data object NavigateToThresholds : WardStatisticsUiEvent
}
