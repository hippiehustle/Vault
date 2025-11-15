package com.skyview.weather.presentation

import androidx.lifecycle.ViewModel
import com.skyview.weather.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * App-level ViewModel for global state.
 *
 * Manages app-wide preferences and state like onboarding completion.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    /**
     * Flow indicating whether onboarding has been completed.
     */
    val onboardingCompleted: Flow<Boolean> = preferencesManager.onboardingCompleted
}
