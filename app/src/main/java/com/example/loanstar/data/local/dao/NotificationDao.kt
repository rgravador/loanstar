package com.example.loanstar.data.local.dao

import androidx.room.*
import com.example.loanstar.data.local.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Notification entities
 */
@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: String): NotificationEntity?

    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    fun getNotificationByIdFlow(notificationId: String): Flow<NotificationEntity?>

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getNotificationsByUser(userId: String): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsByUserFlow(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = :isRead ORDER BY timestamp DESC")
    suspend fun getNotificationsByUserAndReadStatus(userId: String, isRead: Boolean): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = :isRead ORDER BY timestamp DESC")
    fun getNotificationsByUserAndReadStatusFlow(userId: String, isRead: Boolean): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    suspend fun getNotificationsByUserAndType(userId: String, type: String): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND priority = :priority ORDER BY timestamp DESC")
    suspend fun getNotificationsByUserAndPriority(userId: String, priority: String): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    suspend fun getUnreadNotificationCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadNotificationCountFlow(userId: String): Flow<Int>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    suspend fun getAllNotifications(): List<NotificationEntity>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotificationById(notificationId: String)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteNotificationsByUser(userId: String)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsReadForUser(userId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)
}
