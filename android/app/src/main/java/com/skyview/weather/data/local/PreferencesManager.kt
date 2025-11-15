package com.skyview.weather.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages app preferences using DataStore.
 *
 * Stores user preferences for theme, units, API keys, and app settings.
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val TEMPERATURE_UNIT_KEY = stringPreferencesKey("temperature_unit")
        private val WIND_SPEED_UNIT_KEY = stringPreferencesKey("wind_speed_unit")
        private val PRESSURE_UNIT_KEY = stringPreferencesKey("pressure_unit")
        private val WEATHER_API_KEY = stringPreferencesKey("weather_api_key")
        private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
        private val AUTO_LOCK_TIMEOUT_KEY = intPreferencesKey("auto_lock_timeout")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val VAULT_INITIALIZED_KEY = booleanPreferencesKey("vault_initialized")
    }

    /**
     * Theme preference.
     */
    val theme: Flow<Theme> = context.dataStore.data.map { preferences ->
        when (preferences[THEME_KEY]) {
            "light" -> Theme.LIGHT
            "dark" -> Theme.DARK
            else -> Theme.SYSTEM
        }
    }

    suspend fun setTheme(theme: Theme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name.lowercase()
        }
    }

    /**
     * Temperature unit preference.
     */
    val temperatureUnit: Flow<TemperatureUnit> = context.dataStore.data.map { preferences ->
        when (preferences[TEMPERATURE_UNIT_KEY]) {
            "celsius" -> TemperatureUnit.CELSIUS
            else -> TemperatureUnit.FAHRENHEIT
        }
    }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        context.dataStore.edit { preferences ->
            preferences[TEMPERATURE_UNIT_KEY] = unit.name.lowercase()
        }
    }

    /**
     * Wind speed unit preference.
     */
    val windSpeedUnit: Flow<WindSpeedUnit> = context.dataStore.data.map { preferences ->
        when (preferences[WIND_SPEED_UNIT_KEY]) {
            "kph" -> WindSpeedUnit.KPH
            "ms" -> WindSpeedUnit.MS
            else -> WindSpeedUnit.MPH
        }
    }

    suspend fun setWindSpeedUnit(unit: WindSpeedUnit) {
        context.dataStore.edit { preferences ->
            preferences[WIND_SPEED_UNIT_KEY] = unit.name.lowercase()
        }
    }

    /**
     * Pressure unit preference.
     */
    val pressureUnit: Flow<PressureUnit> = context.dataStore.data.map { preferences ->
        when (preferences[PRESSURE_UNIT_KEY]) {
            "mbar" -> PressureUnit.MBAR
            "mmhg" -> PressureUnit.MMHG
            else -> PressureUnit.INHG
        }
    }

    suspend fun setPressureUnit(unit: PressureUnit) {
        context.dataStore.edit { preferences ->
            preferences[PRESSURE_UNIT_KEY] = unit.name.lowercase()
        }
    }

    /**
     * Weather API key preference.
     */
    val weatherApiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[WEATHER_API_KEY]
    }

    suspend fun setWeatherApiKey(key: String?) {
        context.dataStore.edit { preferences ->
            if (key != null) {
                preferences[WEATHER_API_KEY] = key
            } else {
                preferences.remove(WEATHER_API_KEY)
            }
        }
    }

    /**
     * Biometric authentication preference.
     */
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BIOMETRIC_ENABLED_KEY] ?: false
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }

    /**
     * Auto-lock timeout preference (in minutes).
     */
    val autoLockTimeout: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[AUTO_LOCK_TIMEOUT_KEY] ?: 5
    }

    suspend fun setAutoLockTimeout(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_LOCK_TIMEOUT_KEY] = minutes
        }
    }

    /**
     * Onboarding completion status.
     */
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
        }
    }

    /**
     * Vault initialization status.
     */
    val vaultInitialized: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[VAULT_INITIALIZED_KEY] ?: false
    }

    suspend fun setVaultInitialized(initialized: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VAULT_INITIALIZED_KEY] = initialized
        }
    }
}

/**
 * Theme options.
 */
enum class Theme {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Temperature unit options.
 */
enum class TemperatureUnit {
    FAHRENHEIT,
    CELSIUS
}

/**
 * Wind speed unit options.
 */
enum class WindSpeedUnit {
    MPH,
    KPH,
    MS
}

/**
 * Pressure unit options.
 */
enum class PressureUnit {
    INHG,
    MBAR,
    MMHG
}
