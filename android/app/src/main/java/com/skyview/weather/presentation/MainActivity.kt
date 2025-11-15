package com.skyview.weather.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.skyview.weather.core.security.BiometricManager
import com.skyview.weather.presentation.theme.SkyViewTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main Activity for SkyView Weather app.
 *
 * Entry point for the application. Handles deep links for widget tap sequences
 * and vault access.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var biometricManager: BiometricManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SkyViewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SkyViewApp(biometricManager = biometricManager)
                }
            }
        }

        // Handle deep link intents (from widget taps)
        handleDeepLink()
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink()
    }

    /**
     * Handles deep link navigation from widget taps.
     */
    private fun handleDeepLink() {
        intent?.data?.let { uri ->
            when (uri.host) {
                "tap" -> {
                    // Handle tap sequence recording
                    val tapId = uri.pathSegments.firstOrNull()
                    // Tap sequence will be handled by TapSequenceTracker
                }
                "vault" -> {
                    // Navigate to vault (after authentication)
                }
            }
        }
    }
}
