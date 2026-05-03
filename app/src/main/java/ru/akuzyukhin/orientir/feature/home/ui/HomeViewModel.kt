package ru.akuzyukhin.orientir.feature.home.ui

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
import ru.akuzyukhin.orientir.core.data.storage.TokenStorage
import ru.akuzyukhin.orientir.core.ui.toUserMessage
import ru.akuzyukhin.orientir.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/** ViewModel главного экрана */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val session = tokenStorage.getCurrentSession()
            if (session == null) {
                _events.send(HomeUiEvent.NavigateToLogin)
            } else {
                _uiState.update {
                    it.copy(userId = session.userId, role = session.role)
                }
            }
        }
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, errorMessage = null) }

            authRepository.logout()
                .onSuccess {
                    _events.send(HomeUiEvent.NavigateToLogin)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            errorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }
}