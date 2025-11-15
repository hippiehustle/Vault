package com.skyview.weather.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.skyview.weather.core.security.BiometricManager
import com.skyview.weather.presentation.navigation.SkyViewNavigation

/**
 * Main app composable with navigation.
 */
@Composable
fun SkyViewApp(
    biometricManager: BiometricManager
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        SkyViewNavigation(
            biometricManager = biometricManager,
            activity = activity
        )
    }
}
