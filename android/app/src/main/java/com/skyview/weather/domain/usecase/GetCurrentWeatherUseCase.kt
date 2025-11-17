package com.skyview.weather.domain.usecase

import com.skyview.weather.data.repository.WeatherRepository
import com.skyview.weather.domain.model.Weather
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting current weather.
 *
 * Retrieves current weather conditions for a specific location from the repository.
 * Results are cached and can be force-refreshed if needed.
 */
class GetCurrentWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    /**
     * Gets current weather for specified coordinates.
     *
     * @param latitude Location latitude in degrees
     * @param longitude Location longitude in degrees
     * @param forceRefresh If true, bypasses cache and fetches fresh data
     * @return Flow emitting Result containing Weather data or error
     */
    operator fun invoke(
        latitude: Double,
        longitude: Double,
        forceRefresh: Boolean = false
    ): Flow<Result<Weather>> {
        return weatherRepository.getCurrentWeather(latitude, longitude, forceRefresh)
    }
}
