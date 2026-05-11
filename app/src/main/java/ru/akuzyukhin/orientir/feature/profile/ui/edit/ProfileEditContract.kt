package ru.akuzyukhin.orientir.feature.profile.ui.edit

import ru.akuzyukhin.orientir.core.ui.UiEvent
import ru.akuzyukhin.orientir.feature.profile.domain.model.Profile

/** Состояние экрана редактирования профиля */
data class ProfileEditUiState(
    val initialProfile: Profile? = null,

    // Текущие значения полей формы
    val surname: String = "",
    val name: String = "",
    val patronymic: String = "",
    val email: String = "",
    val address: String = "",

    // Состояния загрузки
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val loadErrorMessage: String? = null,
    val saveErrorMessage: String? = null,

    // Ошибки валидации полей
    val surnameError: String? = null,
    val nameError: String? = null,
    val patronymicError: String? = null,
    val emailError: String? = null
) {
    val hasChanges: Boolean
        get() {
            val initial = initialProfile ?: return false
            return surname.trim() != initial.surname ||
                    name.trim() != initial.name ||
                    patronymic.trim() != (initial.patronymic ?: "") ||
                    email.trim() != (initial.email ?: "") ||
                    address.trim() != (initial.address ?: "")
        }

    val isSaveEnabled: Boolean
        get() = !isSaving && initialProfile != null && hasChanges
}

sealed interface ProfileEditUiEvent : UiEvent {
    data object NavigateBack : ProfileEditUiEvent
}