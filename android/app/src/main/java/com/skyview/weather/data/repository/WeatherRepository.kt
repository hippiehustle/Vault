package com.skyview.weather.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyview.weather.BuildConfig
import com.skyview.weather.core.network.WeatherApiService
import com.skyview.weather.data.model.*
import com.skyview.weather.domain.model.*
import com.skyview.weather.util.Constants
import com.skyview.weather.util.WeatherUnits
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for weather data.
 *
 * Handles fetching weather data from API with caching and error handling.
 *
 * @property apiService Weather API service
 * @property dataStore DataStore for preferences
 * @property context Application context
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: WeatherApiService,
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY_API_KEY = stringPreferencesKey(Constants.PREF_API_KEY)
        private val KEY_UNITS = stringPreferencesKey(Constants.PREF_UNITS)
    }

    /**
     * Gets current weather for a location.
     *
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param forceRefresh Force fetch from API (skip cache)
     * @return Flow of Result with Weather data
     */
    fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
        forceRefresh: Boolean = false
    ): Flow<Result<Weather>> = flow {
        try {
            val apiKey = getApiKey()
            val units = getUnits()

            val response = apiService.getCurrentWeather(
                latitude,
                longitude,
                apiKey,
                units.name.lowercase()
            )

            if (response.isSuccessful && response.body() != null) {
                val weather = response.body()!!.toWeather()
                emit(Result.success(weather))
            } else {
                emit(Result.failure(Exception("Failed to fetch weather: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Gets weather forecast.
     *
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @return Flow of Result with forecast data
     */
    fun getForecast(
        latitude: Double,
        longitude: Double
    ): Flow<Result<WeatherForecast>> = flow {
        try {
            val apiKey = getApiKey()
            val units = getUnits()

            // Try to use One Call API first (if available)
            val currentResponse = apiService.getCurrentWeather(
                latitude,
                longitude,
                apiKey,
                units.name.lowercase()
            )

            val forecastResponse = apiService.getForecast(
                latitude,
                longitude,
                apiKey,
                units.name.lowercase()
            )

            if (currentResponse.isSuccessful && currentResponse.body() != null &&
                forecastResponse.isSuccessful && forecastResponse.body() != null
            ) {
                val current = currentResponse.body()!!.toWeather()
                val forecast = forecastResponse.body()!!

                val hourly = forecast.list?.take(24)?.map { it.toHourlyForecast() } ?: emptyList()
                val daily = forecast.list?.groupByDay()?.take(7) ?: emptyList()

                val weatherForecast = WeatherForecast(
                    current = current,
                    hourly = hourly,
                    daily = daily,
                    alerts = emptyList() // Alerts require One Call API
                )

                emit(Result.success(weatherForecast))
            } else {
                emit(Result.failure(Exception("Failed to fetch forecast")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Gets API key from preferences or returns default.
     */
    private suspend fun getApiKey(): String {
        return dataStore.data.first()[KEY_API_KEY] ?: BuildConfig.DEFAULT_WEATHER_API_KEY
    }

    /**
     * Gets units preference.
     */
    private suspend fun getUnits(): WeatherUnits {
        val unitsString = dataStore.data.first()[KEY_UNITS] ?: WeatherUnits.IMPERIAL.name
        return WeatherUnits.valueOf(unitsString)
    }

    /**
     * Saves API key to preferences.
     */
    suspend fun saveApiKey(apiKey: String) {
        dataStore.edit { prefs ->
            prefs[KEY_API_KEY] = apiKey
        }
    }

    /**
     * Saves units preference.
     */
    suspend fun saveUnits(units: WeatherUnits) {
        dataStore.edit { prefs ->
            prefs[KEY_UNITS] = units.name
        }
    }

    /**
     * Converts WeatherResponse to Weather domain model.
     */
    private fun WeatherResponse.toWeather(): Weather {
        return Weather(
            locationName = cityName ?: "Unknown",
            latitude = coordinates?.latitude ?: 0.0,
            longitude = coordinates?.longitude ?: 0.0,
            temperature = main?.temperature ?: 0.0,
            feelsLike = main?.feelsLike ?: 0.0,
            tempMin = main?.tempMin ?: 0.0,
            tempMax = main?.tempMax ?: 0.0,
            condition = weather?.firstOrNull()?.main ?: "Unknown",
            conditionDescription = weather?.firstOrNull()?.description ?: "Unknown",
            conditionIcon = weather?.firstOrNull()?.icon ?: "01d",
            humidity = main?.humidity ?: 0,
            pressure = main?.pressure ?: 0,
            windSpeed = wind?.speed ?: 0.0,
            windDirection = wind?.degrees,
            cloudiness = clouds?.cloudiness ?: 0,
            visibility = visibility,
            uvIndex = null, // Not available in basic API
            sunrise = sys?.sunrise,
            sunset = sys?.sunset,
            timestamp = timestamp ?: System.currentTimeMillis() / 1000
        )
    }

    /**
     * Converts ForecastItem to HourlyForecast.
     */
    private fun ForecastItem.toHourlyForecast(): HourlyForecast {
        return HourlyForecast(
            timestamp = timestamp,
            temperature = main.temperature,
            feelsLike = main.feelsLike,
            condition = weather.firstOrNull()?.main ?: "Unknown",
            conditionIcon = weather.firstOrNull()?.icon ?: "01d",
            precipitationProbability = precipitationProbability ?: 0.0,
            humidity = main.humidity,
            windSpeed = wind.speed
        )
    }

    /**
     * Groups forecast items by day and creates daily forecasts.
     */
    private fun List<ForecastItem>.groupByDay(): List<DailyForecast> {
        return groupBy { item ->
            // Group by date (ignore time)
            val date = java.util.Date(item.timestamp * 1000)
            val calendar = java.util.Calendar.getInstance().apply {
                time = date
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            calendar.timeInMillis / 1000
        }.map { (dayTimestamp, items) ->
            val temps = items.map { it.main.temperature }
            val dayItem = items.find { it.sys?.partOfDay == "d" } ?: items.first()
            val nightItem = items.find { it.sys?.partOfDay == "n" } ?: items.last()

            DailyForecast(
                date = dayTimestamp,
                tempMin = temps.minOrNull() ?: 0.0,
                tempMax = temps.maxOrNull() ?: 0.0,
                tempDay = dayItem.main.temperature,
                tempNight = nightItem.main.temperature,
                condition = dayItem.weather.firstOrNull()?.main ?: "Unknown",
                conditionDescription = dayItem.weather.firstOrNull()?.description ?: "Unknown",
                conditionIcon = dayItem.weather.firstOrNull()?.icon ?: "01d",
                precipitationProbability = items.maxOfOrNull { it.precipitationProbability ?: 0.0 } ?: 0.0,
                humidity = items.map { it.main.humidity }.average().toInt(),
                windSpeed = items.map { it.wind.speed }.average(),
                uvIndex = null,
                sunrise = 0L, // Not available in forecast API
                sunset = 0L
            )
        }
    }
}
