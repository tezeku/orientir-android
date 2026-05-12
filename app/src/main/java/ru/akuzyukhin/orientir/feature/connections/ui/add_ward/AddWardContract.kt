package ru.akuzyukhin.orientir.feature.connections.ui.add_ward

import ru.akuzyukhin.orientir.core.ui.UiEvent

data class AddWardUiState(
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val phoneError: String? = null,
    val errorMessage: String? = null
) {
    val isSubmitEnabled: Boolean
        get() = !isLoading && phoneNumber.isNotBlank()
}

sealed interface AddWardUiEvent : UiEvent {
    data object NavigateBackWithSuccess : AddWardUiEvent
}