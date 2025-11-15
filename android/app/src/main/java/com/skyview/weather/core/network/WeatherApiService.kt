package com.skyview.weather.core.network

import com.skyview.weather.data.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Weather API service interface for Retrofit.
 *
 * Provides endpoints for fetching weather data from OpenWeatherMap API.
 */
interface WeatherApiService {

    /**
     * Gets current weather for a location.
     *
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param apiKey API key
     * @param units Units (imperial, metric, standard)
     * @return Current weather response
     */
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): Response<WeatherResponse>

    /**
     * Gets 5-day / 3-hour forecast.
     *
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param apiKey API key
     * @param units Units (imperial, metric, standard)
     * @param count Number of forecast items
     * @return Forecast response
     */
    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial",
        @Query("cnt") count: Int? = null
    ): Response<ForecastResponse>

    /**
     * Gets comprehensive weather data (current + forecast + alerts).
     * Note: This endpoint requires a paid API key in OpenWeatherMap.
     *
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param apiKey API key
     * @param units Units (imperial, metric, standard)
     * @param exclude Parts to exclude (comma-separated: current,minutely,hourly,daily,alerts)
     * @return One Call API response
     */
    @GET("onecall")
    suspend fun getOneCallData(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial",
        @Query("exclude") exclude: String? = null
    ): Response<OneCallResponse>
}
