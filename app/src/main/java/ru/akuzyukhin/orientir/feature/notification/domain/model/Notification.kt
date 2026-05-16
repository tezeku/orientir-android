package ru.akuzyukhin.orientir.feature.notification.domain.model

import java.time.LocalDateTime

data class Notification(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val body: String,
    val comment: String?,
    val taskExecutionId: Long?,
    val sentAt: LocalDateTime?,
    val isRead: Boolean
)

data class NotificationsPage(
    val notifications: List<Notification>,
    val page: Int,
    val totalPages: Int,
    val totalElements: Long
)