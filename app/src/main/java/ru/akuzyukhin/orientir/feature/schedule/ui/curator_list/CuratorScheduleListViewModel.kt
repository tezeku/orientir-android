package ru.akuzyukhin.orientir.feature.schedule.ui.curator_list

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
import ru.akuzyukhin.orientir.feature.connections.domain.repository.ConnectionsRepository
import javax.inject.Inject

@HiltViewModel
class CuratorScheduleListViewModel @Inject constructor(
    private val connectionsRepository: ConnectionsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CuratorScheduleListUiState())
    val uiState: StateFlow<CuratorScheduleListUiState> = _uiState.asStateFlow()

    private val _events = Channel<CuratorScheduleListUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { load() }

    fun refresh() = load()

    fun onWardClick(wardId: Long) {
        viewModelScope.launch {
            _events.send(CuratorScheduleListUiEvent.NavigateToWardSchedules(wardId))
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            connectionsRepository.getWards().fold(
                onSuccess = { wards ->
                    _uiState.update { it.copy(isLoading = false, wards = wards) }
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