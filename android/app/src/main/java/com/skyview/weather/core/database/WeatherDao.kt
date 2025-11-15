package com.skyview.weather.core.database

import androidx.room.*
import com.skyview.weather.data.model.ForecastCacheEntity
import com.skyview.weather.data.model.SavedLocationEntity
import com.skyview.weather.data.model.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for weather cache.
 */
@Dao
interface WeatherCacheDao {

    /**
     * Gets cached weather for a location.
     */
    @Query("""
        SELECT * FROM weather_cache
        WHERE latitude BETWEEN :lat - 0.01 AND :lat + 0.01
        AND longitude BETWEEN :lon - 0.01 AND :lon + 0.01
        ORDER BY cached_at DESC
        LIMIT 1
    """)
    suspend fun getWeatherForLocation(lat: Double, lon: Double): WeatherCacheEntity?

    /**
     * Inserts or updates weather cache.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherCacheEntity)

    /**
     * Deletes expired cache entries.
     */
    @Query("DELETE FROM weather_cache WHERE cached_at < :cutoffTime")
    suspend fun deleteExpiredCache(cutoffTime: Long)

    /**
     * Deletes all weather cache.
     */
    @Query("DELETE FROM weather_cache")
    suspend fun clearCache()
}

/**
 * Data Access Object for forecast cache.
 */
@Dao
interface ForecastCacheDao {

    /**
     * Gets cached hourly forecast for a location.
     */
    @Query("""
        SELECT * FROM forecast_cache
        WHERE latitude BETWEEN :lat - 0.01 AND :lat + 0.01
        AND longitude BETWEEN :lon - 0.01 AND :lon + 0.01
        AND forecast_type = 'hourly'
        ORDER BY timestamp ASC
    """)
    suspend fun getHourlyForecast(lat: Double, lon: Double): List<ForecastCacheEntity>

    /**
     * Gets cached daily forecast for a location.
     */
    @Query("""
        SELECT * FROM forecast_cache
        WHERE latitude BETWEEN :lat - 0.01 AND :lat + 0.01
        AND longitude BETWEEN :lon - 0.01 AND :lon + 0.01
        AND forecast_type = 'daily'
        ORDER BY timestamp ASC
    """)
    suspend fun getDailyForecast(lat: Double, lon: Double): List<ForecastCacheEntity>

    /**
     * Inserts forecast entries.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecasts(forecasts: List<ForecastCacheEntity>)

    /**
     * Deletes forecast for a location.
     */
    @Query("""
        DELETE FROM forecast_cache
        WHERE latitude BETWEEN :lat - 0.01 AND :lat + 0.01
        AND longitude BETWEEN :lon - 0.01 AND :lon + 0.01
    """)
    suspend fun deleteForecastForLocation(lat: Double, lon: Double)

    /**
     * Deletes expired forecasts.
     */
    @Query("DELETE FROM forecast_cache WHERE cached_at < :cutoffTime")
    suspend fun deleteExpiredForecasts(cutoffTime: Long)

    /**
     * Clears all forecasts.
     */
    @Query("DELETE FROM forecast_cache")
    suspend fun clearCache()
}

/**
 * Data Access Object for saved locations.
 */
@Dao
interface SavedLocationDao {

    /**
     * Gets all saved locations.
     */
    @Query("SELECT * FROM saved_locations ORDER BY order_index, created_at")
    fun getAllLocations(): Flow<List<SavedLocationEntity>>

    /**
     * Gets default location.
     */
    @Query("SELECT * FROM saved_locations WHERE is_default = 1 LIMIT 1")
    suspend fun getDefaultLocation(): SavedLocationEntity?

    /**
     * Gets location by ID.
     */
    @Query("SELECT * FROM saved_locations WHERE id = :id")
    suspend fun getLocationById(id: String): SavedLocationEntity?

    /**
     * Inserts a location.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SavedLocationEntity)

    /**
     * Updates a location.
     */
    @Update
    suspend fun updateLocation(location: SavedLocationEntity)

    /**
     * Deletes a location.
     */
    @Delete
    suspend fun deleteLocation(location: SavedLocationEntity)

    /**
     * Sets a location as default (and unsets others).
     */
    @Transaction
    suspend fun setDefaultLocation(id: String) {
        clearDefaultFlag()
        setDefaultFlag(id)
    }

    @Query("UPDATE saved_locations SET is_default = 0")
    suspend fun clearDefaultFlag()

    @Query("UPDATE saved_locations SET is_default = 1 WHERE id = :id")
    suspend fun setDefaultFlag(id: String)

    /**
     * Gets location count.
     */
    @Query("SELECT COUNT(*) FROM saved_locations")
    suspend fun getLocationCount(): Int
}
