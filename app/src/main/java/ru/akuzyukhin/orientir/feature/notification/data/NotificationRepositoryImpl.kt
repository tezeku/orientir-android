package ru.akuzyukhin.orientir.feature.notification.data

import android.os.Build
import androidx.annotation.RequiresApi
import ru.akuzyukhin.orientir.feature.notification.domain.model.NotificationsPage
import ru.akuzyukhin.orientir.feature.notification.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val api: NotificationApi
) : NotificationRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getNotifications(page: Int, size: Int, isRead: Boolean?): Result<NotificationsPage> =
        runCatching { api.getNotifications(page, size, isRead).toDomain() }

    override suspend fun markAsRead(notificationId: Long, isRead: Boolean): Result<Unit> =
        runCatching { api.markAsRead(notificationId, MarkReadRequestDto(isRead)); Unit }

    override suspend fun markAllAsRead(): Result<Int> =
        runCatching { api.markAllAsRead().updatedCount }

    override suspend fun getUnreadCount(): Result<Long> =
        runCatching { api.getUnreadCount().unreadCount }
}