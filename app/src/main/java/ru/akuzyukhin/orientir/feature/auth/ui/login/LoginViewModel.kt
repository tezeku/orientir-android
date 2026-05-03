package ru.akuzyukhin.orientir.feature.auth.ui.login

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
import ru.akuzyukhin.orientir.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

/** ViewModel экрана логина */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        LoginUiState(
            phoneNumber = savedStateHandle.get<String>(NAV_ARG_PHONE).orEmpty()
        )
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onPhoneNumberChanged(value: String) {
        _uiState.update {
            it.copy(
                phoneNumber = value,
                phoneError = null,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = null,
                errorMessage = null
            )
        }
    }

    fun onLoginClick() {
        val state = _uiState.value
        if (state.phoneNumber.isBlank() || state.password.isBlank()) {
            _uiState.update {
                it.copy(
                    phoneError = if (state.phoneNumber.isBlank()) "Введите телефон" else null,
                    passwordError = if (state.password.isBlank()) "Введите пароль" else null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.login(state.phoneNumber, state.password)
                .onSuccess { session ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(LoginUiEvent.NavigateToHome(session.role))
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }

    fun onRegisterClick() {
        viewModelScope.launch {
            _events.send(LoginUiEvent.NavigateToRegister)
        }
    }

    companion object {
        const val NAV_ARG_PHONE = "phone"
    }
}