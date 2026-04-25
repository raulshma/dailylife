package com.raulshma.dailylife.di

import android.content.Context
import com.raulshma.dailylife.data.DailyLifeRepository
import com.raulshma.dailylife.data.FileBackedDailyLifeRepository
import com.raulshma.dailylife.notifications.AndroidReminderScheduler
import com.raulshma.dailylife.notifications.ReminderScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DailyLifeModule {
    @Provides
    @Singleton
    fun provideDailyLifeRepository(
        @ApplicationContext context: Context,
    ): DailyLifeRepository {
        val storeFile = File(context.filesDir, "dailylife/local-store.properties")
        return FileBackedDailyLifeRepository(storeFile)
    }

    @Provides
    @Singleton
    fun provideReminderScheduler(
        @ApplicationContext context: Context,
    ): ReminderScheduler = AndroidReminderScheduler(context)
}
