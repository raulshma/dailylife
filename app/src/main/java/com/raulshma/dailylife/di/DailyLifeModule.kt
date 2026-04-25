package com.raulshma.dailylife.di

import android.content.Context
import androidx.room.Room
import com.raulshma.dailylife.data.DailyLifeRepository
import com.raulshma.dailylife.data.RoomBackedDailyLifeRepository
import com.raulshma.dailylife.data.db.DailyLifeDatabase
import com.raulshma.dailylife.notifications.AndroidReminderScheduler
import com.raulshma.dailylife.notifications.ReminderScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DailyLifeModule {
    @Provides
    @Singleton
    fun provideDailyLifeDatabase(
        @ApplicationContext context: Context,
    ): DailyLifeDatabase {
        return Room.databaseBuilder(
            context,
            DailyLifeDatabase::class.java,
            "dailylife.db",
        ).build()
    }

    @Provides
    @Singleton
    fun provideDailyLifeRepository(
        database: DailyLifeDatabase,
    ): DailyLifeRepository {
        return RoomBackedDailyLifeRepository(database)
    }

    @Provides
    @Singleton
    fun provideReminderScheduler(
        @ApplicationContext context: Context,
    ): ReminderScheduler = AndroidReminderScheduler(context)
}
