package ru.akuzyukhin.orientir.feature.connections.ui.add_ward

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
import ru.akuzyukhin.orientir.feature.connections.domain.repository.ConnectionsRepository
import javax.inject.Inject

/** ViewModel экрана привязки подопечного по номеру телефона */
@HiltViewModel
class AddWardViewModel @Inject constructor(
    private val connectionsRepository: ConnectionsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWardUiState())
    val uiState: StateFlow<AddWardUiState> = _uiState.asStateFlow()

    private val _events = Channel<AddWardUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onPhoneNumberChanged(value: String) {
        _uiState.update {
            it.copy(phoneNumber = value, phoneError = null, errorMessage = null)
        }
    }

    fun onSubmit() {
        val state = _uiState.value
        val phoneError = validatePhone(state.phoneNumber)

        if (phoneError != null) {
            _uiState.update { it.copy(phoneError = phoneError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            connectionsRepository.addWard(state.phoneNumber.trim())
                .onSuccess {
                    _events.send(AddWardUiEvent.NavigateBackWithSuccess)
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

    private fun validatePhone(phone: String): String? = when {
        phone.isBlank() -> "Введите номер телефона"
        phone.length > 16 -> "Не больше 16 символов"
        !phone.matches(Regex("^\\+[1-9]\\d{6,14}$")) ->
            "Формат: +код страны и номер, например +79161234567"
        else -> null
    }
}