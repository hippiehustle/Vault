package com.skyview.weather.core.location

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.*
import com.skyview.weather.util.hasPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Provides location services for weather functionality.
 *
 * Handles GPS location detection and geocoding.
 *
 * @property context Application context
 */
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geocoder = Geocoder(context, Locale.getDefault())

    /**
     * Location result.
     */
    data class LocationResult(
        val latitude: Double,
        val longitude: Double,
        val locationName: String
    )

    /**
     * Gets current location with name.
     *
     * @return LocationResult or null if failed
     */
    suspend fun getCurrentLocation(): Result<LocationResult> {
        return try {
            if (!hasLocationPermission()) {
                return Result.failure(SecurityException("Location permission not granted"))
            }

            val location = getLastKnownLocation()
                ?: return Result.failure(Exception("Unable to get location"))

            val locationName = getLocationName(location.latitude, location.longitude)
                ?: "Unknown Location"

            val result = LocationResult(
                latitude = location.latitude,
                longitude = location.longitude,
                locationName = locationName
            )

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets last known location.
     */
    @Suppress("MissingPermission")
    private suspend fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null

        return try {
            // Try to get last known location
            val lastLocation = fusedLocationClient.lastLocation.await()

            if (lastLocation != null && !isLocationOld(lastLocation)) {
                lastLocation
            } else {
                // Request fresh location
                requestNewLocation()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Requests a fresh location update.
     */
    @Suppress("MissingPermission")
    private suspend fun requestNewLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 seconds
        ).apply {
            setMaxUpdates(1)
            setWaitForAccurateLocation(false)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                continuation.resume(result.lastLocation)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            context.mainLooper
        )

        continuation.invokeOnCancellation {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * Gets location name from coordinates using reverse geocoding.
     */
    private suspend fun getLocationName(latitude: Double, longitude: Double): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        val name = addresses.firstOrNull()?.let { address ->
                            address.locality ?: address.subAdminArea ?: address.adminArea
                        }
                        continuation.resume(name)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.let { address ->
                    address.locality ?: address.subAdminArea ?: address.adminArea
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Searches for location by name.
     */
    suspend fun searchLocation(query: String): Result<LocationResult> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocationName(query, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        if (address != null) {
                            val result = LocationResult(
                                latitude = address.latitude,
                                longitude = address.longitude,
                                locationName = address.locality ?: address.adminArea ?: query
                            )
                            continuation.resume(Result.success(result))
                        } else {
                            continuation.resume(Result.failure(Exception("Location not found")))
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 1)
                val address = addresses?.firstOrNull()
                    ?: return Result.failure(Exception("Location not found"))

                val result = LocationResult(
                    latitude = address.latitude,
                    longitude = address.longitude,
                    locationName = address.locality ?: address.adminArea ?: query
                )
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Checks if location permission is granted.
     */
    fun hasLocationPermission(): Boolean {
        return context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    /**
     * Checks if location is too old (> 5 minutes).
     */
    private fun isLocationOld(location: Location): Boolean {
        val age = System.currentTimeMillis() - location.time
        return age > 5 * 60 * 1000 // 5 minutes
    }
}
