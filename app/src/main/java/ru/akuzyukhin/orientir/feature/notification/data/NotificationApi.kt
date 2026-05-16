package ru.akuzyukhin.orientir.feature.notification.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {

    @GET("users/me/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("isRead") isRead: Boolean? = null
    ): NotificationsPageDto

    @PATCH("users/me/notifications/{id}")
    suspend fun markAsRead(
        @Path("id") id: Long,
        @Body request: MarkReadRequestDto
    ): NotificationDto

    @POST("users/me/notifications/read-all")
    suspend fun markAllAsRead(): MarkAllReadResponseDto

    @GET("users/me/notifications/unread-count")
    suspend fun getUnreadCount(): UnreadCountDto
}