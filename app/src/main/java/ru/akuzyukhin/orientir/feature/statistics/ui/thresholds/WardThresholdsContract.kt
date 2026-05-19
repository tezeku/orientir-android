package ru.akuzyukhin.orientir.feature.statistics.ui.thresholds

import ru.akuzyukhin.orientir.core.ui.UiEvent

data class WardThresholdsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
    val periodDays: Int = 7,
    val maxGlobalDeviationPercent: Int = 70,
    val minCompletionRatePercent: Int = 70,
    val maxOverdueRatePercent: Int = 30,
    val maxAvgDeviationMinutes: Int = 30
)

sealed interface WardThresholdsUiEvent : UiEvent {
    data object NavigateBack : WardThresholdsUiEvent
    data object SavedSuccessfully : WardThresholdsUiEvent
}