package ru.akuzyukhin.orientir.feature.connections.ui.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.core.data.storage.TokenStorage
import ru.akuzyukhin.orientir.core.ui.toUserMessage
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role
import ru.akuzyukhin.orientir.feature.connections.domain.model.WardSummary
import ru.akuzyukhin.orientir.feature.connections.domain.repository.ConnectionsRepository
import javax.inject.Inject

@HiltViewModel
class ConnectionsViewModel @Inject constructor(
    private val connectionsRepository: ConnectionsRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionsUiState())
    val uiState: StateFlow<ConnectionsUiState> = _uiState.asStateFlow()

    private val _events = Channel<ConnectionsUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadConnections()
    }

    private fun loadConnections() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val session = tokenStorage.getCurrentSession()
            if (session == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Сессия не найдена"
                    )
                }
                return@launch
            }

            val role = session.role
            _uiState.update { it.copy(role = role) }

            fetchByRole(role) { result ->
                _uiState.update { state ->
                    result.fold(
                        onSuccess = { (wards, curators) ->
                            state.copy(
                                wards = wards,
                                curators = curators,
                                isLoading = false
                            )
                        },
                        onFailure = { error ->
                            state.copy(
                                isLoading = false,
                                errorMessage = error.toUserMessage()
                            )
                        }
                    )
                }
            }
        }
    }

    fun refresh() {
        val role = _uiState.value.role ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            val minDelay = launch { delay(500) }

            val result = fetchByRoleAndReturn(role)
            minDelay.join()

            _uiState.update { state ->
                result.fold(
                    onSuccess = { (wards, curators) ->
                        state.copy(
                            wards = wards,
                            curators = curators,
                            isRefreshing = false
                        )
                    },
                    onFailure = {
                        state.copy(isRefreshing = false)
                    }
                )
            }
        }
    }

    fun retry() = loadConnections()

    fun onAddWardClick() {
        viewModelScope.launch { _events.send(ConnectionsUiEvent.NavigateToAddWard) }
    }

    fun onWardClick(ward: WardSummary) {
        viewModelScope.launch {
            _events.send(ConnectionsUiEvent.NavigateToWardDetails(ward.id))
        }
    }

    fun onWardRemoveClick(ward: WardSummary) {
        _uiState.update { it.copy(wardToRemove = ward) }
    }

    fun onCancelRemove() {
        _uiState.update { it.copy(wardToRemove = null) }
    }

    fun onConfirmRemove() {
        val ward = _uiState.value.wardToRemove ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isRemoving = true) }

            connectionsRepository.removeWard(ward.id)
                .onSuccess {
                    // Локально убираем из списка, без полной перезагрузки.
                    _uiState.update {
                        it.copy(
                            wards = it.wards.filterNot { w -> w.id == ward.id },
                            wardToRemove = null,
                            isRemoving = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isRemoving = false,
                            wardToRemove = null,
                            errorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }

    fun reloadAfterAdd() {
        refresh()
    }

    private suspend inline fun fetchByRole(
        role: Role,
        block: (Result<Pair<List<WardSummary>, List<ru.akuzyukhin.orientir.feature.connections.domain.model.CuratorSummary>>>) -> Unit
    ) {
        block(fetchByRoleAndReturn(role))
    }

    private suspend fun fetchByRoleAndReturn(
        role: Role
    ): Result<Pair<List<WardSummary>, List<ru.akuzyukhin.orientir.feature.connections.domain.model.CuratorSummary>>> {
        return when (role) {
            Role.CURATOR -> connectionsRepository.getWards().map { it to emptyList() }
            Role.WARD -> connectionsRepository.getCurators().map { emptyList<WardSummary>() to it }
        }
    }
}