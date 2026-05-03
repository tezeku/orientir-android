package ru.akuzyukhin.orientir.feature.auth.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.core.data.storage.TokenStorage
import javax.inject.Inject

/** ViewModel экрана-загрузки */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStorage: TokenStorage
) : ViewModel() {
    private val _events = Channel<SplashUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val minDelay = launch { delay(600) }

            val session = tokenStorage.getCurrentSession()

            minDelay.join()  // ждём, пока истечёт минимум

            if (session != null) {
                _events.send(SplashUiEvent.NavigateToHome(session.role))
            } else {
                _events.send(SplashUiEvent.NavigateToLogin)
            }
        }
    }
}