package com.skyview.weather.util

/**
 * Application-wide constants.
 */
object Constants {

    // API Configuration
    const val WEATHER_API_BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val WEATHER_API_FALLBACK_URL = "https://api.weatherapi.com/v1/"

    // Cache Configuration
    const val WEATHER_CACHE_DURATION_MS = 30 * 60 * 1000L // 30 minutes
    const val WIDGET_UPDATE_INTERVAL_MS = 30 * 60 * 1000L // 30 minutes

    // Weather Data Configuration
    const val HOURLY_FORECAST_COUNT = 24
    const val DAILY_FORECAST_COUNT = 7

    // Location Configuration
    const val LOCATION_REQUEST_INTERVAL_MS = 10000L // 10 seconds
    const val LOCATION_MAX_AGE_MS = 5 * 60 * 1000L // 5 minutes

    // Worker Configuration
    const val WEATHER_UPDATE_INTERVAL_MINUTES = 30
    const val WEATHER_UPDATE_FLEX_INTERVAL_MINUTES = 5
    const val WEATHER_WORKER_MAX_RETRY_ATTEMPTS = 3

    // Database Configuration
    const val DATABASE_NAME = "weather_cache.db" // Obfuscated name
    const val VAULT_DATABASE_NAME = "vault.db"

    // Encryption Configuration
    const val ARGON2_MEMORY_KB = 65536 // 64 MB
    const val ARGON2_ITERATIONS = 2
    const val ARGON2_PARALLELISM = 4
    const val PBKDF2_ITERATIONS_COUNT = 100000
    const val SALT_LENGTH_BYTES = 16
    const val AES_KEY_SIZE = 256
    const val GCM_IV_SIZE = 12
    const val GCM_TAG_SIZE = 128

    // Security Configuration
    const val PASSWORD_MIN_LENGTH = 8
    const val TAP_SEQUENCE_TIMEOUT_MS = 5000L // 5 seconds
    const val TAP_SEQUENCE_MAX_LENGTH = 5
    const val TAP_BUFFER_SIZE = 10
    const val MAX_FAILED_BIOMETRIC_ATTEMPTS = 5
    const val MAX_FAILED_SEQUENCE_ATTEMPTS = 3
    const val BIOMETRIC_LOCKOUT_DURATION_MS = 5 * 60 * 1000L // 5 minutes
    const val SEQUENCE_ATTEMPT_WINDOW_MS = 60 * 60 * 1000L // 1 hour

    // Vault Configuration
    const val MAX_SAVED_LOCATIONS = 5
    const val VAULT_MAX_FOLDER_DEPTH = 3
    const val TRASH_RETENTION_DAYS = 30
    const val THUMBNAIL_MAX_SIZE = 200 // pixels
    const val FILE_CHUNK_SIZE = 4096 // 4KB for encryption
    const val MEMORY_CACHE_SIZE_MB = 50

    // Clipboard Security
    const val CLIPBOARD_CLEAR_DELAY_MS = 30 * 1000L // 30 seconds

    // Preferences Keys
    const val PREF_API_KEY = "weather_api_key"
    const val PREF_LOCATION_LAT = "location_latitude"
    const val PREF_LOCATION_LON = "location_longitude"
    const val PREF_LOCATION_NAME = "location_name"
    const val PREF_UNITS = "units"
    const val PREF_THEME = "theme"
    const val PREF_ONBOARDING_COMPLETE = "onboarding_complete"
    const val PREF_VAULT_INITIALIZED = "vault_initialized"
    const val PREF_BIOMETRIC_ENABLED = "biometric_enabled"
    const val PREF_AUTO_LOCK_TIMEOUT = "auto_lock_timeout"
    const val PREF_TAP_SEQUENCES = "tap_sequences"

    // Preferences Default Values
    const val PREF_DEFAULT_AUTO_LOCK_TIMEOUT = 5 // minutes

    // Deep Link Schemes
    const val DEEP_LINK_SCHEME = "skyview"
    const val DEEP_LINK_TAP = "tap"
    const val DEEP_LINK_VAULT = "vault"

    // Widget Actions
    const val ACTION_WIDGET_TAP_1 = "com.skyview.weather.WIDGET_TAP_1"
    const val ACTION_WIDGET_TAP_2 = "com.skyview.weather.WIDGET_TAP_2"
    const val ACTION_WIDGET_TAP_3 = "com.skyview.weather.WIDGET_TAP_3"
    const val ACTION_WIDGET_TAP_4 = "com.skyview.weather.WIDGET_TAP_4"
    const val ACTION_WIDGET_REFRESH = "com.skyview.weather.WIDGET_REFRESH"

    // Notification Configuration
    const val NOTIFICATION_CHANNEL_WEATHER = "weather_alerts"
    const val NOTIFICATION_CHANNEL_VAULT = "vault_security"
}

/**
 * Weather units enumeration.
 */
enum class WeatherUnits {
    IMPERIAL,
    METRIC
}

/**
 * App theme options.
 */
enum class AppTheme {
    LIGHT,
    DARK,
    AUTO
}

/**
 * Auto-lock timeout options (in milliseconds).
 */
enum class AutoLockTimeout(val milliseconds: Long) {
    IMMEDIATE(0),
    THIRTY_SECONDS(30 * 1000),
    ONE_MINUTE(60 * 1000),
    FIVE_MINUTES(5 * 60 * 1000)
}

/**
 * Vault item types.
 */
enum class VaultItemType {
    PHOTO,
    VIDEO,
    DOCUMENT,
    NOTE,
    PASSWORD,
    AUDIO,
    CONTACT
}

/**
 * Widget tap targets for sequence authentication.
 */
enum class TapTarget {
    CLOUD_ICON,
    TEMPERATURE,
    HOURLY_CHART,
    WIND_ICON,
    HUMIDITY_ICON,
    UV_ICON,
    LOCATION_NAME,
    HIGH_LOW_TEMP
}
