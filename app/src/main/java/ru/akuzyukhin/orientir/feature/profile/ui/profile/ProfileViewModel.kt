package ru.akuzyukhin.orientir.feature.profile.ui.profile

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
import ru.akuzyukhin.orientir.feature.auth.domain.repository.AuthRepository
import ru.akuzyukhin.orientir.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

/** ViewModel экрана просмотра профиля */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<ProfileUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadProfile()
    }

    /** Первичная загрузка */
    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            profileRepository.getProfile()
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(profile = profile, isLoading = false)
                    }
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

    /** Обновление по pull-to-refresh или после возврата с экрана редактирования */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            val minDelay = launch { kotlinx.coroutines.delay(500) }

            val result = profileRepository.getProfile()

            minDelay.join()

            result
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(profile = profile, isRefreshing = false)
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
        }
    }

    /** Повтор загрузки после ошибки */
    fun retry() {
        loadProfile()
    }

    fun onEditClick() {
        viewModelScope.launch { _events.send(ProfileUiEvent.NavigateToEdit) }
    }

    fun onChangePasswordClick() {
        viewModelScope.launch { _events.send(ProfileUiEvent.NavigateToChangePassword) }
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }

            authRepository.logout()
                .onSuccess {
                    _events.send(ProfileUiEvent.NavigateToLogin)
                }
                .onFailure {
                    _uiState.update { it.copy(isLoggingOut = false) }
                }
        }
    }
}