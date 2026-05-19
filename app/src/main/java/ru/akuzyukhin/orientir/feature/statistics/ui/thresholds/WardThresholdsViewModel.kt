package ru.akuzyukhin.orientir.feature.statistics.ui.thresholds

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.core.ui.toUserMessage
import ru.akuzyukhin.orientir.feature.statistics.domain.repository.StatisticsRepository
import ru.akuzyukhin.orientir.navigation.HomeTabRoutes
import javax.inject.Inject

@HiltViewModel
class WardThresholdsViewModel @Inject constructor(
    private val repository: StatisticsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wardId: Long = savedStateHandle.get<Long>(HomeTabRoutes.WARD_ID_ARG)!!

    private val _uiState = MutableStateFlow(WardThresholdsUiState())
    val uiState: StateFlow<WardThresholdsUiState> = _uiState.asStateFlow()

    private val _events = Channel<WardThresholdsUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { load() }

    fun onBackClick() {
        viewModelScope.launch { _events.send(WardThresholdsUiEvent.NavigateBack) }
    }

    fun onPeriodChange(value: Int) =
        _uiState.update { it.copy(periodDays = value) }

    fun onGlobalDeviationChange(value: Int) =
        _uiState.update { it.copy(maxGlobalDeviationPercent = value) }

    fun onCompletionChange(value: Int) =
        _uiState.update { it.copy(minCompletionRatePercent = value) }

    fun onOverdueChange(value: Int) =
        _uiState.update { it.copy(maxOverdueRatePercent = value) }

    fun onAvgDeviationChange(value: Int) =
        _uiState.update { it.copy(maxAvgDeviationMinutes = value) }

    fun save() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            repository.updateThresholds(
                wardId = wardId,
                periodDays = s.periodDays,
                maxGlobalDeviationPercent = s.maxGlobalDeviationPercent,
                minCompletionRatePercent = s.minCompletionRatePercent,
                maxOverdueRatePercent = s.maxOverdueRatePercent,
                maxAvgDeviationMinutes = s.maxAvgDeviationMinutes
            ).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, saved = true) }
                    _events.send(WardThresholdsUiEvent.SavedSuccessfully)
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = e.toUserMessage())
                    }
                }
            )
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getThresholds(wardId).fold(
                onSuccess = { t ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            periodDays = t.periodDays,
                            maxGlobalDeviationPercent = t.maxGlobalDeviationPercent,
                            minCompletionRatePercent = t.minCompletionRatePercent,
                            maxOverdueRatePercent = t.maxOverdueRatePercent,
                            maxAvgDeviationMinutes = t.maxAvgDeviationMinutes
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.toUserMessage())
                    }
                }
            )
        }
    }
}