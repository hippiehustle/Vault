package com.skyview.weather.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Database entities for weather data caching.
 */

/**
 * Cached current weather data.
 */
@Entity(
    tableName = "weather_cache",
    indices = [
        Index("latitude", "longitude"),
        Index("cached_at")
    ]
)
data class WeatherCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val location_name: String,
    val temperature: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val condition: String,
    val condition_description: String,
    val condition_icon: String,
    val humidity: Int,
    val pressure: Int,
    val wind_speed: Double,
    val wind_direction: Int?,
    val cloudiness: Int,
    val visibility: Int?,
    val uv_index: Double?,
    val sunrise: Long?,
    val sunset: Long?,
    val timestamp: Long,
    val cached_at: Long = System.currentTimeMillis()
) {
    /**
     * Checks if cached data is expired (older than 30 minutes).
     */
    fun isExpired(): Boolean {
        val age = System.currentTimeMillis() - cached_at
        return age > 30 * 60 * 1000 // 30 minutes
    }
}

/**
 * Cached forecast data.
 */
@Entity(
    tableName = "forecast_cache",
    indices = [
        Index("latitude", "longitude"),
        Index("cached_at")
    ]
)
data class ForecastCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val temperature: Double,
    val feels_like: Double,
    val condition: String,
    val condition_icon: String,
    val precipitation_probability: Double,
    val humidity: Int,
    val wind_speed: Double,
    val forecast_type: String, // "hourly" or "daily"
    val cached_at: Long = System.currentTimeMillis()
) {
    fun isExpired(): Boolean {
        val age = System.currentTimeMillis() - cached_at
        return age > 30 * 60 * 1000
    }
}

/**
 * Saved locations for quick access.
 */
@Entity(
    tableName = "saved_locations",
    indices = [Index("is_default")]
)
data class SavedLocationEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val is_default: Boolean = false,
    val order_index: Int = 0,
    val created_at: Long = System.currentTimeMillis()
)
