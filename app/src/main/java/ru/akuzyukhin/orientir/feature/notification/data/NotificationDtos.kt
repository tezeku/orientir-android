package ru.akuzyukhin.orientir.feature.notification.data

import kotlinx.serialization.Serializable
import ru.akuzyukhin.orientir.feature.notification.domain.model.NotificationType

@Serializable
data class NotificationDto(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val body: String,
    val comment: String? = null,
    val taskExecutionId: Long? = null,
    val sentAt: String? = null,
    val isRead: Boolean = false
)

@Serializable
data class NotificationsPageDto(
    val content: List<NotificationDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

@Serializable
data class MarkReadRequestDto(val isRead: Boolean)

@Serializable
data class UnreadCountDto(val unreadCount: Long)

@Serializable
data class MarkAllReadResponseDto(val updatedCount: Int)