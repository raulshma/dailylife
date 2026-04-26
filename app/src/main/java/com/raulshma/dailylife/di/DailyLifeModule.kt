package com.raulshma.dailylife.di

import android.content.Context
import androidx.room.Room
import com.raulshma.dailylife.data.DailyLifeRepository
import com.raulshma.dailylife.data.RoomBackedDailyLifeRepository
import com.raulshma.dailylife.data.RoomDailyLifeStore
import com.raulshma.dailylife.data.backup.OkHttpS3BackupRepository
import com.raulshma.dailylife.data.backup.S3BackupRepository
import com.raulshma.dailylife.data.db.DailyLifeDatabase
import com.raulshma.dailylife.data.db.DatabasePassphraseManager
import com.raulshma.dailylife.data.db.MIGRATION_1_2
import com.raulshma.dailylife.data.media.AudioWaveformGenerator
import com.raulshma.dailylife.data.media.MediaThumbnailGenerator
import com.raulshma.dailylife.data.security.BackupEncryptionManager
import com.raulshma.dailylife.data.security.MediaEncryptionManager
import com.raulshma.dailylife.notifications.AndroidReminderScheduler
import com.raulshma.dailylife.notifications.ReminderScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DailyLifeModule {
    @Provides
    @Singleton
    fun provideDatabasePassphraseManager(
        @ApplicationContext context: Context,
    ): DatabasePassphraseManager = DatabasePassphraseManager(context)

    @Provides
    @Singleton
    fun provideDailyLifeDatabase(
        @ApplicationContext context: Context,
        passphraseManager: DatabasePassphraseManager,
    ): DailyLifeDatabase {
        val factory = SupportOpenHelperFactory(passphraseManager.getPassphrase(), null, false)
        return Room.databaseBuilder(
            context,
            DailyLifeDatabase::class.java,
            "dailylife.db",
        )
            .openHelperFactory(factory)
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideRoomDailyLifeStore(
        database: DailyLifeDatabase,
    ): RoomDailyLifeStore = RoomDailyLifeStore(database)

    @Provides
    @Singleton
    fun provideDailyLifeRepository(
        database: DailyLifeDatabase,
    ): DailyLifeRepository {
        return RoomBackedDailyLifeRepository(database)
    }

    @Provides
    @Singleton
    fun provideMediaEncryptionManager(
        @ApplicationContext context: Context,
    ): MediaEncryptionManager = MediaEncryptionManager(context)

    @Provides
    @Singleton
    fun provideMediaThumbnailGenerator(
        @ApplicationContext context: Context,
    ): MediaThumbnailGenerator = MediaThumbnailGenerator(context)

    @Provides
    @Singleton
    fun provideAudioWaveformGenerator(): AudioWaveformGenerator = AudioWaveformGenerator()

    @Provides
    @Singleton
    fun provideBackupEncryptionManager(): BackupEncryptionManager = BackupEncryptionManager()

    @Provides
    @Singleton
    fun provideS3BackupRepository(
        encryptionManager: BackupEncryptionManager,
    ): S3BackupRepository = OkHttpS3BackupRepository(encryptionManager)

    @Provides
    @Singleton
    fun provideReminderScheduler(
        @ApplicationContext context: Context,
    ): ReminderScheduler = AndroidReminderScheduler(context)
}
