package com.skyview.weather.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Extension functions for common operations.
 */

/**
 * Check if a permission is granted.
 */
fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * Format timestamp to readable date string.
 */
fun Long.toDateString(pattern: String = "MMM d, yyyy"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(Date(this))
}

/**
 * Format timestamp to time string.
 */
fun Long.toTimeString(pattern: String = "h:mm a"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(Date(this))
}

/**
 * Check if password meets complexity requirements.
 */
fun String.isValidPassword(): Boolean {
    if (length < Constants.PASSWORD_MIN_LENGTH) return false

    val hasUppercase = any { it.isUpperCase() }
    val hasLowercase = any { it.isLowerCase() }
    val hasDigit = any { it.isDigit() }
    val hasSpecial = any { !it.isLetterOrDigit() }

    return hasUppercase && hasLowercase && hasDigit && hasSpecial
}

/**
 * Calculate password strength score (0-4).
 */
fun String.passwordStrength(): Int {
    var score = 0

    if (length >= 8) score++
    if (length >= 12) score++
    if (any { it.isUpperCase() } && any { it.isLowerCase() }) score++
    if (any { it.isDigit() }) score++
    if (any { !it.isLetterOrDigit() }) score++

    return minOf(score, 4)
}

/**
 * Convert bytes to human-readable file size.
 */
fun Long.toFileSizeString(): String {
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024

    return when {
        this >= gb -> String.format("%.2f GB", this / gb)
        this >= mb -> String.format("%.2f MB", this / mb)
        this >= kb -> String.format("%.2f KB", this / kb)
        else -> "$this B"
    }
}

/**
 * Zero out a ByteArray for security.
 */
fun ByteArray.clear() {
    for (i in indices) {
        this[i] = 0
    }
}

/**
 * Convert Celsius to Fahrenheit.
 */
fun Double.celsiusToFahrenheit(): Double = (this * 9.0 / 5.0) + 32.0

/**
 * Convert Fahrenheit to Celsius.
 */
fun Double.fahrenheitToCelsius(): Double = (this - 32.0) * 5.0 / 9.0

/**
 * Convert meters per second to miles per hour.
 */
fun Double.mpsToMph(): Double = this * 2.23694

/**
 * Convert meters per second to kilometers per hour.
 */
fun Double.mpsToKph(): Double = this * 3.6
