package ru.akuzyukhin.orientir.feature.connections.ui.ward_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.core.ui.toUserMessage
import ru.akuzyukhin.orientir.feature.connections.domain.repository.ConnectionsRepository
import javax.inject.Inject

@HiltViewModel
class WardDetailViewModel @Inject constructor(
    private val connectionsRepository: ConnectionsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wardId: Long = savedStateHandle.get<Long>("wardId")!!

    private val _uiState = MutableStateFlow(WardDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<WardDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { loadWard() }

    private fun loadWard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            connectionsRepository.getWard(wardId).fold(
                onSuccess = { ward ->
                    _uiState.update { it.copy(isLoading = false, ward = ward) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) }
                }
            )
        }
    }

    fun removeWard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRemoving = true) }
            connectionsRepository.removeWard(wardId).fold(
                onSuccess = { _events.send(WardDetailEvent.WardRemoved) },
                onFailure = { e ->
                    _uiState.update { it.copy(isRemoving = false) }
                    _events.send(WardDetailEvent.ShowError(e.toUserMessage()))
                }
            )
        }
    }
}
