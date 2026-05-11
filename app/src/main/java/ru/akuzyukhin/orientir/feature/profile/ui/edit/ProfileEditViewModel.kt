package ru.akuzyukhin.orientir.feature.profile.ui.edit

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
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role
import ru.akuzyukhin.orientir.feature.profile.domain.model.ProfileUpdate
import ru.akuzyukhin.orientir.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

/** ViewModel экрана редактирования */
@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    private val _events = Channel<ProfileEditUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadErrorMessage = null) }

            profileRepository.getProfile()
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            initialProfile = profile,
                            surname = profile.surname,
                            name = profile.name,
                            patronymic = profile.patronymic.orEmpty(),
                            email = profile.email.orEmpty(),
                            address = profile.address.orEmpty(),
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadErrorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }

    fun retry() = loadProfile()

    fun onSurnameChanged(value: String) {
        _uiState.update { it.copy(surname = value, surnameError = null, saveErrorMessage = null) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, saveErrorMessage = null) }
    }

    fun onPatronymicChanged(value: String) {
        _uiState.update { it.copy(patronymic = value, patronymicError = null, saveErrorMessage = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, saveErrorMessage = null) }
    }

    fun onAddressChanged(value: String) {
        _uiState.update { it.copy(address = value, saveErrorMessage = null) }
    }

    fun onSave() {
        val state = _uiState.value
        val initial = state.initialProfile ?: return

        val surnameError = validate(state.surname, "Введите фамилию", maxLen = 100)
        val nameError = validate(state.name, "Введите имя", maxLen = 100)
        val patronymicError = if (state.patronymic.length > 100)
            "Не больше 100 символов" else null
        val emailError = if (initial.role == Role.CURATOR && state.email.isBlank())
            "Email обязателен для куратора" else null

        if (listOf(surnameError, nameError, patronymicError, emailError).any { it != null }) {
            _uiState.update {
                it.copy(
                    surnameError = surnameError,
                    nameError = nameError,
                    patronymicError = patronymicError,
                    emailError = emailError
                )
            }
            return
        }

        val update = ProfileUpdate(
            surname = state.surname.trim().takeIf { it != initial.surname },
            name = state.name.trim().takeIf { it != initial.name },
            patronymic = state.patronymic.trim()
                .takeIf { it != (initial.patronymic ?: "") },
            email = if (initial.role == Role.CURATOR) {
                state.email.trim().takeIf { it != (initial.email ?: "") }
            } else null,
            address = if (initial.role == Role.WARD) {
                state.address.trim().takeIf { it != (initial.address ?: "") }
            } else null
        )

        if (!update.hasChanges) {
            viewModelScope.launch { _events.send(ProfileEditUiEvent.NavigateBack) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveErrorMessage = null) }

            profileRepository.updateProfile(update)
                .onSuccess {
                    _events.send(ProfileEditUiEvent.NavigateBack)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveErrorMessage = error.toUserMessage()
                        )
                    }
                }
        }
    }

    private fun validate(value: String, emptyMessage: String, maxLen: Int): String? = when {
        value.isBlank() -> emptyMessage
        value.length > maxLen -> "Не больше $maxLen символов"
        else -> null
    }
}