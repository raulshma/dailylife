package com.raulshma.dailylife.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.raulshma.dailylife.data.DailyLifeRepository
import com.raulshma.dailylife.data.RoomBackedDailyLifeRepository
import com.raulshma.dailylife.data.RoomDailyLifeStore
import com.raulshma.dailylife.data.backup.OkHttpS3BackupRepository
import com.raulshma.dailylife.data.backup.S3BackupRepository
import com.raulshma.dailylife.data.db.DailyLifeDatabase
import com.raulshma.dailylife.data.db.DatabasePassphraseManager
import com.raulshma.dailylife.data.db.ALL_MIGRATIONS
import com.raulshma.dailylife.data.media.AudioWaveformGenerator
import com.raulshma.dailylife.data.media.MediaThumbnailGenerator
import com.raulshma.dailylife.data.security.BackupEncryptionManager
import com.raulshma.dailylife.data.security.MediaEncryptionManager
import com.raulshma.dailylife.notifications.AndroidReminderScheduler
import com.raulshma.dailylife.notifications.GeofenceManager
import com.raulshma.dailylife.notifications.ReminderScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DailyLifeModule {
    private const val DatabaseName = "dailylife.db"

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
        fun buildDatabase(passphrase: ByteArray): DailyLifeDatabase {
            val factory = SupportOpenHelperFactory(passphrase, null, false)
            return Room.databaseBuilder(
                context,
                DailyLifeDatabase::class.java,
                DatabaseName,
            )
                .openHelperFactory(factory)
                .addMigrations(*ALL_MIGRATIONS.toTypedArray())
                .build()
        }

        fun deleteDatabaseFiles() {
            context.deleteDatabase(DatabaseName)
            val dbDir = context.getDatabasePath(DatabaseName).parentFile ?: return
            val sidecarNames = listOf("$DatabaseName-shm", "$DatabaseName-wal", "$DatabaseName-journal")
            sidecarNames.forEach { name ->
                runCatching { File(dbDir, name).delete() }
            }
        }

        val initialPassphrase = passphraseManager.getPassphrase()
        val initialDatabase = buildDatabase(initialPassphrase)
        return runCatching {
            initialDatabase.openHelper.writableDatabase
            initialDatabase
        }.getOrElse { error ->
            Log.w("DailyLife", "Database open failed, attempting recovery: ${error.message}")
            runCatching { initialDatabase.close() }
            deleteDatabaseFiles()
            Log.w("DailyLife", "Database files deleted, regenerating passphrase and rebuilding")
            val regeneratedPassphrase = passphraseManager.regeneratePassphrase()
            val recoveredDatabase = buildDatabase(regeneratedPassphrase)
            recoveredDatabase.openHelper.writableDatabase
            Log.w("DailyLife", "Database recovery successful (previous data lost)")
            recoveredDatabase
        }
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

    @Provides
    @Singleton
    fun provideGeofenceManager(
        application: android.app.Application,
    ): GeofenceManager = GeofenceManager(application)
}
