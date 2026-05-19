package ru.akuzyukhin.orientir.feature.statistics.ui.curator_list

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.core.ui.toUserMessage
import ru.akuzyukhin.orientir.feature.connections.domain.repository.ConnectionsRepository
import ru.akuzyukhin.orientir.feature.statistics.domain.repository.StatisticsRepository
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class CuratorStatisticsListViewModel @Inject constructor(
    private val connectionsRepository: ConnectionsRepository,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CuratorStatisticsListUiState())
    val uiState: StateFlow<CuratorStatisticsListUiState> = _uiState.asStateFlow()

    private val _events = Channel<CuratorStatisticsListUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { load() }

    fun refresh() = load(isRefresh = true)

    fun onWardClick(wardId: Long) {
        viewModelScope.launch {
            _events.send(CuratorStatisticsListUiEvent.NavigateToWardStatistics(wardId))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun load(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (isRefresh) it.copy(isRefreshing = true, errorMessage = null)
                else it.copy(isLoading = true, errorMessage = null)
            }

            connectionsRepository.getWards().fold(
                onSuccess = { wards ->
                    val items = coroutineScope {
                        wards.map { ward ->
                            async {
                                val thresholds = statisticsRepository
                                    .getThresholds(ward.id).getOrNull()
                                val deviation = thresholds?.let { t ->
                                    val to = LocalDate.now()
                                    val from = to.minusDays((t.periodDays - 1).toLong())
                                    statisticsRepository
                                        .getGlobalDeviation(ward.id, from, to)
                                        .getOrNull()
                                }
                                WardStatisticsItem(
                                    wardId = ward.id,
                                    displayName = buildName(ward),
                                    deviation = deviation
                                )
                            }
                        }.awaitAll()
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            items = items
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
        }
    }

    private fun buildName(ward: ru.akuzyukhin.orientir.feature.connections.domain.model.WardSummary): String {
        return listOfNotNull(
            ward.surname,
            ward.name,
            ward.patronymic
        ).joinToString(" ").ifBlank { ward.phoneNumber }
    }
}