package ru.akuzyukhin.orientir.feature.notification.data

import android.os.Build
import androidx.annotation.RequiresApi
import ru.akuzyukhin.orientir.feature.notification.domain.model.Notification
import ru.akuzyukhin.orientir.feature.notification.domain.model.NotificationsPage
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
internal fun NotificationDto.toDomain() = Notification(
    id = id,
    type = type,
    title = title,
    body = body,
    comment = comment,
    taskExecutionId = taskExecutionId,
    sentAt = sentAt?.let { LocalDateTime.parse(it) },
    isRead = isRead
)

@RequiresApi(Build.VERSION_CODES.O)
internal fun NotificationsPageDto.toDomain() = NotificationsPage(
    notifications = content.map { it.toDomain() },
    page = page,
    totalPages = totalPages,
    totalElements = totalElements
)