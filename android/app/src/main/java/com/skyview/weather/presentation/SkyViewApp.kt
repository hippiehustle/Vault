package com.skyview.weather.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.skyview.weather.core.security.BiometricManager
import com.skyview.weather.presentation.navigation.Screen
import com.skyview.weather.presentation.navigation.SkyViewNavigation

/**
 * Main app composable with navigation.
 */
@Composable
fun SkyViewApp(
    biometricManager: BiometricManager,
    viewModel: AppViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState(initial = true)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val startDestination = if (onboardingCompleted) {
            Screen.Weather.route
        } else {
            Screen.Onboarding.route
        }

        SkyViewNavigation(
            biometricManager = biometricManager,
            activity = activity,
            startDestination = startDestination
        )
    }
}
