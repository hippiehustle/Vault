package com.skyview.weather.domain.usecase

import com.skyview.weather.data.repository.WeatherRepository
import com.skyview.weather.domain.model.Weather
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting current weather.
 */
class GetCurrentWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    operator fun invoke(
        latitude: Double,
        longitude: Double,
        forceRefresh: Boolean = false
    ): Flow<Result<Weather>> {
        return weatherRepository.getCurrentWeather(latitude, longitude, forceRefresh)
    }
}
