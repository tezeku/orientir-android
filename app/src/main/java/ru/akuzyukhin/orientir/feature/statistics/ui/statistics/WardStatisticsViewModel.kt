package ru.akuzyukhin.orientir.feature.statistics.ui.statistics

import android.os.Build
import androidx.annotation.RequiresApi
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
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class WardStatisticsViewModel @Inject constructor(
    private val repository: StatisticsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wardId: Long = savedStateHandle.get<Long>(HomeTabRoutes.WARD_ID_ARG)!!

    private val _uiState = MutableStateFlow(WardStatisticsUiState())
    val uiState: StateFlow<WardStatisticsUiState> = _uiState.asStateFlow()

    private val _events = Channel<WardStatisticsUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { load() }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refresh() = load(isRefresh = true)

    fun onBackClick() {
        viewModelScope.launch { _events.send(WardStatisticsUiEvent.NavigateBack) }
    }

    fun onThresholdsClick() {
        viewModelScope.launch { _events.send(WardStatisticsUiEvent.NavigateToThresholds) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun load(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (isRefresh) it.copy(isRefreshing = true, errorMessage = null)
                else it.copy(isLoading = true, errorMessage = null)
            }

            repository.getThresholds(wardId).fold(
                onSuccess = { thresholds ->
                    val to = LocalDate.now()
                    val from = to.minusDays((thresholds.periodDays - 1).toLong())
                    repository.getGlobalDeviation(wardId, from, to).fold(
                        onSuccess = { deviation ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    deviation = deviation
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    errorMessage = e.toUserMessage()
                                )
                            }
                        }
                    )
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = e.toUserMessage()
                        )
                    }
                }
            )
        }
    }
}