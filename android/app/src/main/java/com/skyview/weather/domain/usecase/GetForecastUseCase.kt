package com.skyview.weather.domain.usecase

import com.skyview.weather.data.repository.WeatherRepository
import com.skyview.weather.domain.model.WeatherForecast
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting weather forecast.
 */
class GetForecastUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Flow<Result<WeatherForecast>> {
        return weatherRepository.getForecast(latitude, longitude)
    }
}
