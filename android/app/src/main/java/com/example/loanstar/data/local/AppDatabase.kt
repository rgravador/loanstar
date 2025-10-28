package com.example.loanstar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.loanstar.data.local.dao.*
import com.example.loanstar.data.local.entities.*

/**
 * Room Database for Loanstar application
 * Provides offline caching and data persistence
 *
 * Schema matches setup.sql for consistency with Supabase backend
 */
@Database(
    entities = [
        UserEntity::class,
        AccountEntity::class,
        LoanEntity::class,
        PaymentEntity::class,
        NotificationEntity::class,
        EarningsEntity::class,
        CashoutRequestEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // DAOs
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun loanDao(): LoanDao
    abstract fun paymentDao(): PaymentDao
    abstract fun notificationDao(): NotificationDao
    abstract fun earningsDao(): EarningsDao
    abstract fun cashoutRequestDao(): CashoutRequestDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "loanstar_database"
    }
}
