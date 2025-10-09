package com.example.loanstar.di

import android.content.Context
import androidx.room.Room
import com.example.loanstar.data.local.AppDatabase
import com.example.loanstar.data.remote.SupabaseClientProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing application-wide dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClientProvider(): SupabaseClientProvider {
        return SupabaseClientProvider()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "loanstar_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase) = database.userDao()

    @Provides
    @Singleton
    fun provideAccountDao(database: AppDatabase) = database.accountDao()

    @Provides
    @Singleton
    fun provideLoanDao(database: AppDatabase) = database.loanDao()

    @Provides
    @Singleton
    fun providePaymentDao(database: AppDatabase) = database.paymentDao()

    @Provides
    @Singleton
    fun provideNotificationDao(database: AppDatabase) = database.notificationDao()

    @Provides
    @Singleton
    fun provideEarningsDao(database: AppDatabase) = database.earningsDao()

    @Provides
    @Singleton
    fun provideCashoutRequestDao(database: AppDatabase) = database.cashoutRequestDao()

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase) = database.transactionDao()
}
