package com.skyview.weather.core.di

import android.content.Context
import com.skyview.weather.core.database.*
import com.skyview.weather.core.security.KeyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides SkyView database instance.
     * Database is encrypted with SQLCipher using device-specific passphrase.
     *
     * The passphrase is derived from Android Keystore, ensuring each device
     * has a unique database encryption key that cannot be extracted.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyManager: KeyManager
    ): SkyViewDatabase {
        // Generate database passphrase from Android Keystore
        // This creates a unique key per device installation
        val databaseKey = keyManager.getOrCreateDatabaseKey()
        val passphrase = android.util.Base64.encodeToString(
            databaseKey.encoded,
            android.util.Base64.NO_WRAP
        ).toCharArray()

        return SkyViewDatabase.getDatabase(context, passphrase)
    }

    @Provides
    @Singleton
    fun provideVaultItemDao(database: SkyViewDatabase): VaultItemDao {
        return database.vaultItemDao()
    }

    @Provides
    @Singleton
    fun provideVaultFolderDao(database: SkyViewDatabase): VaultFolderDao {
        return database.vaultFolderDao()
    }

    @Provides
    @Singleton
    fun provideVaultSettingsDao(database: SkyViewDatabase): VaultSettingsDao {
        return database.vaultSettingsDao()
    }

    @Provides
    @Singleton
    fun provideVaultTrashDao(database: SkyViewDatabase): VaultTrashDao {
        return database.vaultTrashDao()
    }

    @Provides
    @Singleton
    fun provideWeatherCacheDao(database: SkyViewDatabase): WeatherCacheDao {
        return database.weatherCacheDao()
    }

    @Provides
    @Singleton
    fun provideForecastCacheDao(database: SkyViewDatabase): ForecastCacheDao {
        return database.forecastCacheDao()
    }

    @Provides
    @Singleton
    fun provideSavedLocationDao(database: SkyViewDatabase): SavedLocationDao {
        return database.savedLocationDao()
    }
}
