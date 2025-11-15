package com.skyview.weather.presentation.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.skyview.weather.core.security.BiometricManager
import com.skyview.weather.presentation.onboarding.OnboardingScreen
import com.skyview.weather.presentation.settings.SettingsScreen
import com.skyview.weather.presentation.vault.VaultBrowserScreen
import com.skyview.weather.presentation.vault.VaultItemDetailScreen
import com.skyview.weather.presentation.vault.VaultUnlockScreen
import com.skyview.weather.presentation.vault.VaultViewModel
import com.skyview.weather.presentation.weather.WeatherHomeScreen
import com.skyview.weather.presentation.weather.WeatherViewModel

/**
 * Navigation routes.
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Weather : Screen("weather")
    object VaultUnlock : Screen("vault_unlock")
    object VaultBrowser : Screen("vault_browser")
    object VaultItem : Screen("vault_item/{itemId}") {
        fun createRoute(itemId: String) = "vault_item/$itemId"
    }
    object Settings : Screen("settings")
}

/**
 * Main navigation graph for the app.
 */
@Composable
fun SkyViewNavigation(
    navController: NavHostController = rememberNavController(),
    biometricManager: BiometricManager,
    activity: ComponentActivity?,
    startDestination: String = Screen.Weather.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Onboarding screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Weather.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                biometricManager = biometricManager,
                activity = activity
            )
        }

        // Weather home screen
        composable(Screen.Weather.route) {
            val weatherViewModel: WeatherViewModel = hiltViewModel()

            WeatherHomeScreen(
                viewModel = weatherViewModel,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Vault unlock screen
        composable(Screen.VaultUnlock.route) {
            val vaultViewModel: VaultViewModel = hiltViewModel()

            VaultUnlockScreen(
                viewModel = vaultViewModel,
                biometricManager = biometricManager,
                activity = activity,
                onUnlocked = {
                    navController.navigate(Screen.VaultBrowser.route) {
                        popUpTo(Screen.VaultUnlock.route) { inclusive = true }
                    }
                }
            )
        }

        // Vault browser screen
        composable(Screen.VaultBrowser.route) {
            val vaultViewModel: VaultViewModel = hiltViewModel()

            VaultBrowserScreen(
                viewModel = vaultViewModel,
                onNavigateToItem = { itemId ->
                    navController.navigate(Screen.VaultItem.createRoute(itemId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Vault item detail screen
        composable(Screen.VaultItem.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            if (itemId != null) {
                VaultItemDetailScreen(
                    itemId = itemId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Settings screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
