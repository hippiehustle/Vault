package com.skyview.weather.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.skyview.weather.core.location.LocationProvider
import com.skyview.weather.data.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for periodic weather updates.
 *
 * Updates weather data and widget every 30 minutes.
 */
@HiltWorker
class WeatherUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val locationProvider: LocationProvider
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "weather_update_work"

        /**
         * Schedules periodic weather updates.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val updateRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                30, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES // Flex interval
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                updateRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            // Get current location
            val locationResult = locationProvider.getCurrentLocation()

            locationResult.onSuccess { location ->
                // Fetch weather data
                weatherRepository.getCurrentWeather(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    forceRefresh = true
                ).first()

                // Update widget
                // Widget will automatically refresh when data is updated
            }

            Result.success()
        } catch (e: Exception) {
            // Retry on failure
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
