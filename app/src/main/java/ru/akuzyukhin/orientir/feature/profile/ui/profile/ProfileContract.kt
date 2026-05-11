package ru.akuzyukhin.orientir.feature.profile.ui.profile

import ru.akuzyukhin.orientir.core.ui.UiEvent
import ru.akuzyukhin.orientir.feature.profile.domain.model.Profile

/** Состояние экрана просмотра профиля */
data class ProfileUiState(
    val profile: Profile? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val isLoggingOut: Boolean = false
)

sealed interface ProfileUiEvent : UiEvent {
    data object NavigateToEdit : ProfileUiEvent

    data object NavigateToChangePassword : ProfileUiEvent

    data object NavigateToLogin : ProfileUiEvent
}