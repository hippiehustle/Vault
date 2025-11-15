package com.skyview.weather.domain.model

/**
 * Domain models for weather data.
 * These are clean models used throughout the app, independent of API structure.
 */

/**
 * Current weather data.
 */
data class Weather(
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val condition: String,
    val conditionDescription: String,
    val conditionIcon: String,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val windDirection: Int?,
    val cloudiness: Int,
    val visibility: Int?,
    val uvIndex: Double?,
    val sunrise: Long?,
    val sunset: Long?,
    val timestamp: Long
) {
    /**
     * Gets weather icon resource name based on condition code.
     */
    fun getIconResource(): String {
        return when {
            conditionIcon.contains("01") -> "ic_weather_clear"
            conditionIcon.contains("02") -> "ic_weather_few_clouds"
            conditionIcon.contains("03") || conditionIcon.contains("04") -> "ic_weather_clouds"
            conditionIcon.contains("09") -> "ic_weather_rain"
            conditionIcon.contains("10") -> "ic_weather_rain"
            conditionIcon.contains("11") -> "ic_weather_thunderstorm"
            conditionIcon.contains("13") -> "ic_weather_snow"
            conditionIcon.contains("50") -> "ic_weather_mist"
            else -> "ic_weather_clear"
        }
    }

    /**
     * Checks if it's currently daytime.
     */
    fun isDaytime(): Boolean {
        if (sunrise == null || sunset == null) return true
        val currentTime = System.currentTimeMillis() / 1000
        return currentTime in sunrise..sunset
    }
}

/**
 * Hourly forecast data.
 */
data class HourlyForecast(
    val timestamp: Long,
    val temperature: Double,
    val feelsLike: Double,
    val condition: String,
    val conditionIcon: String,
    val precipitationProbability: Double,
    val humidity: Int,
    val windSpeed: Double
)

/**
 * Daily forecast data.
 */
data class DailyForecast(
    val date: Long,
    val tempMin: Double,
    val tempMax: Double,
    val tempDay: Double,
    val tempNight: Double,
    val condition: String,
    val conditionDescription: String,
    val conditionIcon: String,
    val precipitationProbability: Double,
    val humidity: Int,
    val windSpeed: Double,
    val uvIndex: Double?,
    val sunrise: Long,
    val sunset: Long
)

/**
 * Weather forecast container.
 */
data class WeatherForecast(
    val current: Weather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val alerts: List<WeatherAlertInfo>
)

/**
 * Weather alert information.
 */
data class WeatherAlertInfo(
    val event: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val sender: String
)

/**
 * Saved location.
 */
data class SavedLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean = false,
    val order: Int = 0
)
