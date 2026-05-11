package ru.akuzyukhin.orientir.feature.profile.ui.password

import ru.akuzyukhin.orientir.core.ui.UiEvent

/** Состояние экрана смены пароля */
data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val showPasswords: Boolean = false,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null
) {
    val isSubmitEnabled: Boolean
        get() = !isLoading
                && currentPassword.isNotBlank()
                && newPassword.isNotBlank()
                && confirmPassword.isNotBlank()
}

sealed interface ChangePasswordUiEvent : UiEvent {
    data object NavigateBack : ChangePasswordUiEvent
}