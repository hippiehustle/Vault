package com.skyview.weather.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.skyview.weather.data.model.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

/**
 * Room database for SkyView Weather app.
 *
 * Stores both weather cache and encrypted vault data.
 * Vault tables are encrypted using SQLCipher.
 */
@Database(
    entities = [
        // Vault entities
        VaultItemEntity::class,
        VaultFolderEntity::class,
        VaultSettingsEntity::class,
        VaultTrashEntity::class,
        // Weather entities
        WeatherCacheEntity::class,
        ForecastCacheEntity::class,
        SavedLocationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SkyViewDatabase : RoomDatabase() {

    // Vault DAOs
    abstract fun vaultItemDao(): VaultItemDao
    abstract fun vaultFolderDao(): VaultFolderDao
    abstract fun vaultSettingsDao(): VaultSettingsDao
    abstract fun vaultTrashDao(): VaultTrashDao

    // Weather DAOs
    abstract fun weatherCacheDao(): WeatherCacheDao
    abstract fun forecastCacheDao(): ForecastCacheDao
    abstract fun savedLocationDao(): SavedLocationDao

    companion object {
        @Volatile
        private var INSTANCE: SkyViewDatabase? = null

        /**
         * Gets database instance with SQLCipher encryption.
         *
         * @param context Application context
         * @param passphrase Database encryption passphrase (derived from master key)
         * @return Database instance
         */
        fun getDatabase(context: Context, passphrase: CharArray): SkyViewDatabase {
            return INSTANCE ?: synchronized(this) {
                val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase))

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SkyViewDatabase::class.java,
                    "weather_cache.db" // Obfuscated name for stealth
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration() // For development only
                    .build()

                // Clear passphrase from memory
                for (i in passphrase.indices) {
                    passphrase[i] = 0.toChar()
                }

                INSTANCE = instance
                instance
            }
        }

        /**
         * Closes database and clears instance.
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
