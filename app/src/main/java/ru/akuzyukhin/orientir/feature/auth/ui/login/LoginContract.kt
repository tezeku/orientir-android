package ru.akuzyukhin.orientir.feature.auth.ui.login

import ru.akuzyukhin.orientir.core.ui.UiEvent
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role

/** Состояние экрана логина */
data class LoginUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null
) {
    val isLoginEnabled: Boolean
        get() = phoneNumber.isNotBlank() && password.isNotBlank() && !isLoading
}

/** Одноразовые события, которые ViewModel посылает экрану */
sealed interface LoginUiEvent : UiEvent {
    data class NavigateToHome(val role: Role) : LoginUiEvent

    data object NavigateToRegister : LoginUiEvent
}
