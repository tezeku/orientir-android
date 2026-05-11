package ru.akuzyukhin.orientir.feature.profile.ui.password

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
import ru.akuzyukhin.orientir.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

/** ViewModel экрана смены пароля */
@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    private val _events = Channel<ChangePasswordUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onCurrentPasswordChanged(value: String) {
        _uiState.update {
            it.copy(currentPassword = value, currentPasswordError = null, errorMessage = null)
        }
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                newPassword = value,
                newPasswordError = null,
                confirmPasswordError = null,
                errorMessage = null
            )
        }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update {
            it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null)
        }
    }

    fun onToggleShowPasswords() {
        _uiState.update { it.copy(showPasswords = !it.showPasswords) }
    }

    fun onSubmit() {
        val state = _uiState.value

        val currentError = if (state.currentPassword.isBlank())
            "Введите текущий пароль" else null

        val newError = when {
            state.newPassword.isBlank() -> "Введите новый пароль"
            state.newPassword.length < 8 -> "Минимум 8 символов"
            state.newPassword == state.currentPassword ->
                "Новый пароль должен отличаться от текущего"
            else -> null
        }

        val confirmError = when {
            state.confirmPassword.isBlank() -> "Повторите новый пароль"
            state.confirmPassword != state.newPassword -> "Пароли не совпадают"
            else -> null
        }

        if (listOf(currentError, newError, confirmError).any { it != null }) {
            _uiState.update {
                it.copy(
                    currentPasswordError = currentError,
                    newPasswordError = newError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            profileRepository.changePassword(
                currentPassword = state.currentPassword,
                newPassword = state.newPassword
            )
                .onSuccess {
                    _events.send(ChangePasswordUiEvent.NavigateBack)
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
}