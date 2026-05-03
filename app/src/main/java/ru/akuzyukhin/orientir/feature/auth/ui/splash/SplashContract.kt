package ru.akuzyukhin.orientir.feature.auth.ui.splash

import ru.akuzyukhin.orientir.core.ui.UiEvent
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role

/** События экрана-загрузки */
sealed interface SplashUiEvent : UiEvent {
    data class NavigateToHome(val role: Role) : SplashUiEvent

    data object NavigateToLogin : SplashUiEvent
}