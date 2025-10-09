package com.example.loanstar.data.repository

import com.example.loanstar.data.local.dao.NotificationDao
import com.example.loanstar.data.local.entities.NotificationEntity
import com.example.loanstar.data.local.entities.toDomain
import com.example.loanstar.data.local.entities.toEntity
import com.example.loanstar.data.model.Notification
import com.example.loanstar.data.model.NotificationPriority
import com.example.loanstar.data.model.NotificationType
import com.example.loanstar.data.remote.SupabaseClientProvider
import com.example.loanstar.util.NetworkMonitor
import com.example.loanstar.util.Result
import com.example.loanstar.util.safeCall
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val supabaseClient: SupabaseClientProvider,
    private val notificationDao: NotificationDao,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository
) {

    fun getNotificationsByUserFlow(userId: String): Flow<List<Notification>> {
        return notificationDao.getNotificationsByUserFlow(userId).map { it.map { entity -> entity.toDomain() } }
    }

    fun getUnreadCountFlow(userId: String): Flow<Int> {
        return notificationDao.getUnreadNotificationCountFlow(userId)
    }

    suspend fun createNotification(
        userId: String,
        message: String,
        type: NotificationType,
        relatedEntityId: String? = null,
        relatedEntityType: String? = null,
        priority: NotificationPriority = NotificationPriority.NORMAL
    ): Result<Notification> = safeCall {
        val notification = Notification(
            id = java.util.UUID.randomUUID().toString(),
            userId = userId,
            message = message,
            type = type,
            relatedEntityId = relatedEntityId,
            relatedEntityType = relatedEntityType,
            priority = priority
        )

        val isOnline = networkMonitor.isConnected.first()
        if (isOnline) {
            supabaseClient.postgrest.from("notifications").insert(notification.toEntity())
        }

        notificationDao.insertNotification(notification.toEntity())
        notification
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> = safeCall {
        val isOnline = networkMonitor.isConnected.first()
        if (isOnline) {
            supabaseClient.postgrest.from("notifications")
                .update(mapOf("is_read" to true)) {
                    filter { eq("id", notificationId) }
                }
        }
        notificationDao.markAsRead(notificationId)
    }

    suspend fun markAllAsRead(userId: String): Result<Unit> = safeCall {
        val isOnline = networkMonitor.isConnected.first()
        if (isOnline) {
            supabaseClient.postgrest.from("notifications")
                .update(mapOf("is_read" to true)) {
                    filter { eq("user_id", userId) }
                }
        }
        notificationDao.markAllAsReadForUser(userId)
    }

    suspend fun syncNotifications(userId: String): Result<Unit> = safeCall {
        val isOnline = networkMonitor.isConnected.first()
        if (!isOnline) throw Exception("No network connection")

        val notifications = supabaseClient.postgrest
            .from("notifications")
            .select { filter { eq("user_id", userId) } }
            .decodeList<NotificationEntity>()

        notificationDao.insertNotifications(notifications)
    }
}
