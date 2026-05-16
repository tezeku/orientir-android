package ru.akuzyukhin.orientir.feature.notification.ui.list

import ru.akuzyukhin.orientir.feature.notification.domain.model.Notification

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Long = 0,
    val errorMessage: String? = null
)
