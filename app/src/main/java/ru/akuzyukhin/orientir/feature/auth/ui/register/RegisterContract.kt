package ru.akuzyukhin.orientir.feature.auth.ui.register

import ru.akuzyukhin.orientir.core.ui.UiEvent
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role

/** Состояние экрана регистрации */
data class RegisterUiState(
    // Роль
    val role: Role? = null,

    // Основные поля
    val surname: String = "",
    val name: String = "",
    val patronymic: String = "",
    val phoneNumber: String = "",
    val password: String = "",

    // Дополнительные поля
    val email: String = "",
    val address: String = "",

    // Состояние сети
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Ошибки валидации полей
    val surnameError: String? = null,
    val nameError: String? = null,
    val patronymicError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val emailError: String? = null,

    // Флаг существующего телефона
    val phoneAlreadyTaken: Boolean = false
) {
    val isSubmitEnabled: Boolean
        get() = !isLoading
                && role != null
                && surname.isNotBlank()
                && name.isNotBlank()
                && phoneNumber.isNotBlank()
                && password.isNotBlank()
                && (role != Role.CURATOR || email.isNotBlank())
}

sealed interface RegisterUiEvent : UiEvent {
    data class NavigateToHome(val role: Role) : RegisterUiEvent

    data class NavigateToLogin(val phoneNumber: String? = null) : RegisterUiEvent

    data object NavigateBack : RegisterUiEvent
}

