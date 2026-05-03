package ru.akuzyukhin.orientir.feature.home.ui

import ru.akuzyukhin.orientir.core.ui.UiEvent
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role

/** Состояние главного экрана */
data class HomeUiState(
    val userId: Long? = null,
    val role: Role? = null,
    val isLoggingOut: Boolean = false,
    val errorMessage: String? = null
)

sealed interface HomeUiEvent : UiEvent {
    /** Юзер вышел из системы — навигация на логин. */
    data object NavigateToLogin : HomeUiEvent
}