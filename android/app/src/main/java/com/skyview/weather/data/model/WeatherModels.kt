package com.skyview.weather.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data models for weather API responses.
 */

/**
 * Current weather response from API.
 */
data class WeatherResponse(
    @SerializedName("coord") val coordinates: Coordinates?,
    @SerializedName("weather") val weather: List<WeatherCondition>?,
    @SerializedName("base") val base: String?,
    @SerializedName("main") val main: MainWeatherData?,
    @SerializedName("visibility") val visibility: Int?,
    @SerializedName("wind") val wind: Wind?,
    @SerializedName("clouds") val clouds: Clouds?,
    @SerializedName("rain") val rain: Rain?,
    @SerializedName("snow") val snow: Snow?,
    @SerializedName("dt") val timestamp: Long?,
    @SerializedName("sys") val sys: Sys?,
    @SerializedName("timezone") val timezone: Int?,
    @SerializedName("id") val cityId: Int?,
    @SerializedName("name") val cityName: String?,
    @SerializedName("cod") val code: Int?
)

/**
 * Coordinates data.
 */
data class Coordinates(
    @SerializedName("lon") val longitude: Double,
    @SerializedName("lat") val latitude: Double
)

/**
 * Weather condition.
 */
data class WeatherCondition(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

/**
 * Main weather data (temperature, pressure, humidity).
 */
data class MainWeatherData(
    @SerializedName("temp") val temperature: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("sea_level") val seaLevel: Int?,
    @SerializedName("grnd_level") val groundLevel: Int?
)

/**
 * Wind data.
 */
data class Wind(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val degrees: Int?,
    @SerializedName("gust") val gust: Double?
)

/**
 * Cloud coverage.
 */
data class Clouds(
    @SerializedName("all") val cloudiness: Int
)

/**
 * Rain data.
 */
data class Rain(
    @SerializedName("1h") val oneHour: Double?,
    @SerializedName("3h") val threeHours: Double?
)

/**
 * Snow data.
 */
data class Snow(
    @SerializedName("1h") val oneHour: Double?,
    @SerializedName("3h") val threeHours: Double?
)

/**
 * System data (country, sunrise, sunset).
 */
data class Sys(
    @SerializedName("type") val type: Int?,
    @SerializedName("id") val id: Int?,
    @SerializedName("country") val country: String?,
    @SerializedName("sunrise") val sunrise: Long?,
    @SerializedName("sunset") val sunset: Long?
)

/**
 * Forecast response from API.
 */
data class ForecastResponse(
    @SerializedName("cod") val code: String?,
    @SerializedName("message") val message: Int?,
    @SerializedName("cnt") val count: Int?,
    @SerializedName("list") val list: List<ForecastItem>?,
    @SerializedName("city") val city: City?
)

/**
 * Forecast item (3-hour forecast).
 */
data class ForecastItem(
    @SerializedName("dt") val timestamp: Long,
    @SerializedName("main") val main: MainWeatherData,
    @SerializedName("weather") val weather: List<WeatherCondition>,
    @SerializedName("clouds") val clouds: Clouds,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("visibility") val visibility: Int?,
    @SerializedName("pop") val precipitationProbability: Double?,
    @SerializedName("rain") val rain: Rain?,
    @SerializedName("snow") val snow: Snow?,
    @SerializedName("sys") val sys: ForecastSys?,
    @SerializedName("dt_txt") val dateTimeText: String?
)

/**
 * Forecast system data.
 */
data class ForecastSys(
    @SerializedName("pod") val partOfDay: String?
)

/**
 * City information.
 */
data class City(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("coord") val coordinates: Coordinates,
    @SerializedName("country") val country: String,
    @SerializedName("population") val population: Int?,
    @SerializedName("timezone") val timezone: Int?,
    @SerializedName("sunrise") val sunrise: Long?,
    @SerializedName("sunset") val sunset: Long?
)

/**
 * One Call API response (comprehensive weather data).
 */
data class OneCallResponse(
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("timezone_offset") val timezoneOffset: Int,
    @SerializedName("current") val current: CurrentWeather?,
    @SerializedName("hourly") val hourly: List<HourlyWeather>?,
    @SerializedName("daily") val daily: List<DailyWeather>?,
    @SerializedName("alerts") val alerts: List<WeatherAlert>?
)

/**
 * Current weather from One Call API.
 */
data class CurrentWeather(
    @SerializedName("dt") val timestamp: Long,
    @SerializedName("sunrise") val sunrise: Long?,
    @SerializedName("sunset") val sunset: Long?,
    @SerializedName("temp") val temperature: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("dew_point") val dewPoint: Double?,
    @SerializedName("uvi") val uvIndex: Double?,
    @SerializedName("clouds") val clouds: Int,
    @SerializedName("visibility") val visibility: Int?,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("wind_deg") val windDegrees: Int?,
    @SerializedName("wind_gust") val windGust: Double?,
    @SerializedName("weather") val weather: List<WeatherCondition>,
    @SerializedName("rain") val rain: Rain?,
    @SerializedName("snow") val snow: Snow?
)

/**
 * Hourly weather forecast.
 */
data class HourlyWeather(
    @SerializedName("dt") val timestamp: Long,
    @SerializedName("temp") val temperature: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("dew_point") val dewPoint: Double?,
    @SerializedName("uvi") val uvIndex: Double?,
    @SerializedName("clouds") val clouds: Int,
    @SerializedName("visibility") val visibility: Int?,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("wind_deg") val windDegrees: Int?,
    @SerializedName("wind_gust") val windGust: Double?,
    @SerializedName("weather") val weather: List<WeatherCondition>,
    @SerializedName("pop") val precipitationProbability: Double?,
    @SerializedName("rain") val rain: Rain?,
    @SerializedName("snow") val snow: Snow?
)

/**
 * Daily weather forecast.
 */
data class DailyWeather(
    @SerializedName("dt") val timestamp: Long,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long,
    @SerializedName("moonrise") val moonrise: Long?,
    @SerializedName("moonset") val moonset: Long?,
    @SerializedName("moon_phase") val moonPhase: Double?,
    @SerializedName("temp") val temp: DailyTemp,
    @SerializedName("feels_like") val feelsLike: DailyFeelsLike,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("dew_point") val dewPoint: Double?,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("wind_deg") val windDegrees: Int?,
    @SerializedName("wind_gust") val windGust: Double?,
    @SerializedName("weather") val weather: List<WeatherCondition>,
    @SerializedName("clouds") val clouds: Int,
    @SerializedName("pop") val precipitationProbability: Double?,
    @SerializedName("rain") val rain: Double?,
    @SerializedName("snow") val snow: Double?,
    @SerializedName("uvi") val uvIndex: Double?
)

/**
 * Daily temperature data.
 */
data class DailyTemp(
    @SerializedName("day") val day: Double,
    @SerializedName("min") val min: Double,
    @SerializedName("max") val max: Double,
    @SerializedName("night") val night: Double,
    @SerializedName("eve") val evening: Double,
    @SerializedName("morn") val morning: Double
)

/**
 * Daily feels-like temperature data.
 */
data class DailyFeelsLike(
    @SerializedName("day") val day: Double,
    @SerializedName("night") val night: Double,
    @SerializedName("eve") val evening: Double,
    @SerializedName("morn") val morning: Double
)

/**
 * Weather alert.
 */
data class WeatherAlert(
    @SerializedName("sender_name") val senderName: String,
    @SerializedName("event") val event: String,
    @SerializedName("start") val start: Long,
    @SerializedName("end") val end: Long,
    @SerializedName("description") val description: String,
    @SerializedName("tags") val tags: List<String>?
)
