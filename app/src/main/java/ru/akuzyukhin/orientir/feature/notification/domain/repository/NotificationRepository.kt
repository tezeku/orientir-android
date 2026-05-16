package ru.akuzyukhin.orientir.feature.notification.domain.repository

import ru.akuzyukhin.orientir.feature.notification.domain.model.NotificationsPage

interface NotificationRepository {
    suspend fun getNotifications(page: Int = 0, size: Int = 20, isRead: Boolean? = null): Result<NotificationsPage>
    suspend fun markAsRead(notificationId: Long, isRead: Boolean): Result<Unit>
    suspend fun markAllAsRead(): Result<Int>
    suspend fun getUnreadCount(): Result<Long>
}