package ru.akuzyukhin.orientir.feature.notification.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.akuzyukhin.orientir.feature.notification.domain.model.NotificationType

@Serializable
data class NotificationDto(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val body: String,
    val comment: String? = null,
    @SerialName("task_execution_id")
    val taskExecutionId: Long? = null,
    @SerialName("sent_at")
    val sentAt: String? = null,
    @SerialName("is_read")
    val isRead: Boolean = false
)

@Serializable
data class NotificationsPageDto(
    val content: List<NotificationDto> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    @SerialName("total_elements")
    val totalElements: Long = 0,
    @SerialName("total_pages")
    val totalPages: Int = 0
)

@Serializable
data class MarkReadRequestDto(val isRead: Boolean)

@Serializable
data class UnreadCountDto(val unreadCount: Long)

@Serializable
data class MarkAllReadResponseDto(val updatedCount: Int)