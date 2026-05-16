package ru.akuzyukhin.orientir.feature.notification.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.akuzyukhin.orientir.core.ui.toUserMessage
import ru.akuzyukhin.orientir.feature.notification.domain.model.Notification
import ru.akuzyukhin.orientir.feature.notification.domain.repository.NotificationRepository
import javax.inject.Inject

private const val REFRESH_MIN_DURATION_MS = 500L
private const val PAGE_SIZE = 50

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val pageResult = repository.getNotifications(page = 0, size = PAGE_SIZE)
            val countResult = repository.getUnreadCount()

            val notifications = pageResult.getOrElse {
                _uiState.update { s -> s.copy(isLoading = false, errorMessage = it.toUserMessage()) }
                return@launch
            }.notifications
            val count = countResult.getOrDefault(0)

            _uiState.update {
                it.copy(isLoading = false, notifications = notifications, unreadCount = count)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val startTime = System.currentTimeMillis()
            val pageResult = repository.getNotifications(page = 0, size = PAGE_SIZE)
            val countResult = repository.getUnreadCount()
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < REFRESH_MIN_DURATION_MS) delay(REFRESH_MIN_DURATION_MS - elapsed)

            pageResult
                .onSuccess { page ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            notifications = page.notifications,
                            unreadCount = countResult.getOrDefault(it.unreadCount)
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isRefreshing = false, errorMessage = error.toUserMessage()) }
                }
        }
    }

    fun retry() = load()

    fun onNotificationClick(notification: Notification) {
        if (notification.isRead) return
        viewModelScope.launch {
            repository.markAsRead(notification.id, true)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            notifications = it.notifications.map { n ->
                                if (n.id == notification.id) n.copy(isRead = true) else n
                            },
                            unreadCount = (it.unreadCount - 1).coerceAtLeast(0)
                        )
                    }
                }
        }
    }

    fun onMarkAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map { it.copy(isRead = true) },
                            unreadCount = 0
                        )
                    }
                }
        }
    }
}