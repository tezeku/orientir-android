package ru.akuzyukhin.orientir.feature.auth.ui.register

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
import retrofit2.HttpException
import ru.akuzyukhin.orientir.core.data.network.serverMessage
import ru.akuzyukhin.orientir.core.ui.toUserMessage
import ru.akuzyukhin.orientir.feature.auth.domain.model.RegistrationData
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role
import ru.akuzyukhin.orientir.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel(){
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onRoleSelected(role: Role) {
        _uiState.update { it.copy(role = role, errorMessage = null) }
    }

    fun onSurnameChanged(value: String) {
        _uiState.update { it.copy(surname = value, surnameError = null) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun onPatronymicChanged(value: String) {
        _uiState.update { it.copy(patronymic = value, patronymicError = null) }
    }

    fun onPhoneNumberChanged(value: String) {
        _uiState.update {
            it.copy(
                phoneNumber = value,
                phoneError = null,
                phoneAlreadyTaken = false
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onAddressChanged(value: String) {
        _uiState.update { it.copy(address = value) }
    }

    fun onBackClick() {
        viewModelScope.launch { _events.send(RegisterUiEvent.NavigateBack) }
    }

    fun onLoginClick() {
        viewModelScope.launch { _events.send(RegisterUiEvent.NavigateToLogin) }
    }

    fun onSubmit() {
        val state = _uiState.value
        val role = state.role ?: return

        val surnameError = validateRequired(state.surname, "Введите фамилию", maxLen = 100)
        val nameError = validateRequired(state.name, "Введите имя", maxLen = 100)
        val patronymicError = if (state.patronymic.length > 100)
            "Не больше 100 символов" else null
        val phoneError = validatePhone(state.phoneNumber)
        val passwordError = validatePassword(state.password)
        val emailError = if (role == Role.CURATOR)
            validateRequired(state.email, "Email обязателен для куратора") else null

        val hasErrors = listOf(
            surnameError, nameError, patronymicError,
            phoneError, passwordError, emailError
        ).any { it != null }

        if (hasErrors) {
            _uiState.update {
                it.copy(
                    surnameError = surnameError,
                    nameError = nameError,
                    patronymicError = patronymicError,
                    phoneError = phoneError,
                    passwordError = passwordError,
                    emailError = emailError
                )
            }
            return
        }

        val data = RegistrationData(
            surname = state.surname.trim(),
            name = state.name.trim(),
            patronymic = state.patronymic.trim().takeIf { it.isNotBlank() },
            phoneNumber = state.phoneNumber.trim(),
            password = state.password,
            role = role,
            email = state.email.trim().takeIf { it.isNotBlank() },
            address = state.address.trim().takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            authRepository.register(data)
                .onSuccess { session ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(RegisterUiEvent.NavigateToHome(session.role))
                }
                .onFailure { error ->
                    handleRegistrationError(error)
                }
        }
    }

    private fun handleRegistrationError(error: Throwable) {
        val serverMsg = (error as? HttpException)?.serverMessage()

        // Эвристика «телефон уже занят»
        val isPhoneTaken = serverMsg
            ?.contains("уже зарегистрирован", ignoreCase = true) == true

        _uiState.update { state ->
            if (isPhoneTaken) {
                state.copy(
                    isLoading = false,
                    phoneAlreadyTaken = true,
                    errorMessage = null
                )
            } else {
                state.copy(
                    isLoading = false,
                    errorMessage = error.toUserMessage()
                )
            }
        }
    }

    private fun validateRequired(
        value: String,
        emptyMessage: String,
        maxLen: Int? = null
    ): String? = when {
        value.isBlank() -> emptyMessage
        maxLen != null && value.length > maxLen -> "Не больше $maxLen символов"
        else -> null
    }

    private fun validatePhone(phone: String): String? = when {
        phone.isBlank() -> "Введите телефон"
        phone.length > 16 -> "Не больше 16 символов"
        !phone.matches(Regex("^\\+[1-9]\\d{6,14}$")) ->
            "Формат: +код страны и номер, например +79161234567"
        else -> null
    }

    private fun validatePassword(password: String): String? = when {
        password.isBlank() -> "Введите пароль"
        password.length < 8 -> "Минимум 8 символов"
        else -> null
    }
}