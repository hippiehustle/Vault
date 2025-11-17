package com.skyview.weather.domain.usecase

import com.skyview.weather.data.repository.WeatherRepository
import com.skyview.weather.domain.model.WeatherForecast
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting weather forecast.
 *
 * Retrieves multi-day weather forecast for a specific location from the repository.
 * Includes hourly and daily forecast data.
 */
class GetForecastUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    /**
     * Gets weather forecast for specified coordinates.
     *
     * @param latitude Location latitude in degrees
     * @param longitude Location longitude in degrees
     * @return Flow emitting Result containing WeatherForecast with hourly and daily data
     */
    operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Flow<Result<WeatherForecast>> {
        return weatherRepository.getForecast(latitude, longitude)
    }
}
