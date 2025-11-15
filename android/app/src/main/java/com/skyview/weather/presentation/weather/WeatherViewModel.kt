package com.skyview.weather.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skyview.weather.core.location.LocationProvider
import com.skyview.weather.domain.model.Weather
import com.skyview.weather.domain.model.WeatherForecast
import com.skyview.weather.domain.usecase.GetCurrentWeatherUseCase
import com.skyview.weather.domain.usecase.GetForecastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for weather screens.
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val getForecastUseCase: GetForecastUseCase,
    private val locationProvider: LocationProvider
) : ViewModel() {

    /**
     * UI state for weather screen.
     */
    data class WeatherUiState(
        val weather: Weather? = null,
        val forecast: WeatherForecast? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val hasLocationPermission: Boolean = false,
        val currentLocation: LocationProvider.LocationResult? = null
    )

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        checkLocationPermission()
    }

    /**
     * Checks if location permission is granted.
     */
    fun checkLocationPermission() {
        val hasPermission = locationProvider.hasLocationPermission()
        _uiState.update { it.copy(hasLocationPermission = hasPermission) }

        if (hasPermission) {
            loadCurrentLocation()
        }
    }

    /**
     * Loads current location.
     */
    fun loadCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            locationProvider.getCurrentLocation()
                .onSuccess { location ->
                    _uiState.update {
                        it.copy(
                            currentLocation = location,
                            isLoading = false,
                            error = null
                        )
                    }
                    loadWeather(location.latitude, location.longitude)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to get location"
                        )
                    }
                }
        }
    }

    /**
     * Loads weather for coordinates.
     */
    fun loadWeather(latitude: Double, longitude: Double, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load current weather
            getCurrentWeatherUseCase(latitude, longitude, forceRefresh)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load weather"
                        )
                    }
                }
                .collect { result ->
                    result
                        .onSuccess { weather ->
                            _uiState.update {
                                it.copy(
                                    weather = weather,
                                    isLoading = false,
                                    error = null
                                )
                            }
                            // Load forecast
                            loadForecast(latitude, longitude)
                        }
                        .onFailure { error ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to load weather"
                                )
                            }
                        }
                }
        }
    }

    /**
     * Loads forecast.
     */
    private fun loadForecast(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            getForecastUseCase(latitude, longitude)
                .catch { error ->
                    // Forecast is optional, don't show error
                }
                .collect { result ->
                    result.onSuccess { forecast ->
                        _uiState.update { it.copy(forecast = forecast) }
                    }
                }
        }
    }

    /**
     * Refreshes weather.
     */
    fun refresh() {
        _uiState.value.currentLocation?.let { location ->
            loadWeather(location.latitude, location.longitude, forceRefresh = true)
        } ?: loadCurrentLocation()
    }

    /**
     * Searches for location.
     */
    fun searchLocation(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            locationProvider.searchLocation(query)
                .onSuccess { location ->
                    _uiState.update {
                        it.copy(
                            currentLocation = location,
                            isLoading = false,
                            error = null
                        )
                    }
                    loadWeather(location.latitude, location.longitude)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Location not found"
                        )
                    }
                }
        }
    }

    /**
     * Clears error.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
