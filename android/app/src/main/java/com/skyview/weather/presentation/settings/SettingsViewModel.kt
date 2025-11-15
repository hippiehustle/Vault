package com.skyview.weather.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyview.weather.BuildConfig
import com.skyview.weather.data.local.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen.
 *
 * Manages user preferences and app settings.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                preferencesManager.theme,
                preferencesManager.temperatureUnit,
                preferencesManager.windSpeedUnit,
                preferencesManager.pressureUnit,
                preferencesManager.weatherApiKey,
                preferencesManager.biometricEnabled,
                preferencesManager.autoLockTimeout
            ) { theme, tempUnit, windUnit, pressureUnit, apiKey, biometricEnabled, autoLockTimeout ->
                SettingsUiState(
                    theme = theme,
                    temperatureUnit = tempUnit,
                    windSpeedUnit = windUnit,
                    pressureUnit = pressureUnit,
                    customApiKey = apiKey,
                    biometricEnabled = biometricEnabled,
                    autoLockTimeout = autoLockTimeout,
                    appVersion = BuildConfig.VERSION_NAME
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesManager.setTheme(theme)
        }
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        viewModelScope.launch {
            preferencesManager.setTemperatureUnit(unit)
        }
    }

    fun setWindSpeedUnit(unit: WindSpeedUnit) {
        viewModelScope.launch {
            preferencesManager.setWindSpeedUnit(unit)
        }
    }

    fun setPressureUnit(unit: PressureUnit) {
        viewModelScope.launch {
            preferencesManager.setPressureUnit(unit)
        }
    }

    fun setCustomApiKey(key: String?) {
        viewModelScope.launch {
            preferencesManager.setWeatherApiKey(key)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setBiometricEnabled(enabled)
        }
    }

    fun setAutoLockTimeout(minutes: Int) {
        viewModelScope.launch {
            preferencesManager.setAutoLockTimeout(minutes)
        }
    }

    fun showApiKeyDialog() {
        _uiState.update { it.copy(showApiKeyDialog = true) }
    }

    fun hideApiKeyDialog() {
        _uiState.update { it.copy(showApiKeyDialog = false) }
    }

    fun showAutoLockDialog() {
        _uiState.update { it.copy(showAutoLockDialog = true) }
    }

    fun hideAutoLockDialog() {
        _uiState.update { it.copy(showAutoLockDialog = false) }
    }
}

/**
 * UI state for Settings screen.
 */
data class SettingsUiState(
    val theme: Theme = Theme.SYSTEM,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.FAHRENHEIT,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.MPH,
    val pressureUnit: PressureUnit = PressureUnit.INHG,
    val customApiKey: String? = null,
    val biometricEnabled: Boolean = false,
    val autoLockTimeout: Int = 5,
    val appVersion: String = "1.0.0",
    val showApiKeyDialog: Boolean = false,
    val showAutoLockDialog: Boolean = false
)
