package com.skyview.weather.presentation.settings

import com.skyview.weather.data.local.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SettingsViewModel.
 *
 * Tests user preference management and UI state updates.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var preferencesManager: PreferencesManager

    private val testDispatcher = StandardTestDispatcher()

    // Preference flows
    private val themeFlow = MutableStateFlow(Theme.SYSTEM)
    private val temperatureUnitFlow = MutableStateFlow(TemperatureUnit.FAHRENHEIT)
    private val windSpeedUnitFlow = MutableStateFlow(WindSpeedUnit.MPH)
    private val pressureUnitFlow = MutableStateFlow(PressureUnit.INHG)
    private val apiKeyFlow = MutableStateFlow<String?>(null)
    private val biometricEnabledFlow = MutableStateFlow(false)
    private val autoLockTimeoutFlow = MutableStateFlow(5)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        preferencesManager = mockk(relaxed = true)

        // Set up flows
        every { preferencesManager.theme } returns themeFlow
        every { preferencesManager.temperatureUnit } returns temperatureUnitFlow
        every { preferencesManager.windSpeedUnit } returns windSpeedUnitFlow
        every { preferencesManager.pressureUnit } returns pressureUnitFlow
        every { preferencesManager.weatherApiKey } returns apiKeyFlow
        every { preferencesManager.biometricEnabled } returns biometricEnabledFlow
        every { preferencesManager.autoLockTimeout } returns autoLockTimeoutFlow

        // Set up suspend functions
        coEvery { preferencesManager.setTheme(any()) } just Runs
        coEvery { preferencesManager.setTemperatureUnit(any()) } just Runs
        coEvery { preferencesManager.setWindSpeedUnit(any()) } just Runs
        coEvery { preferencesManager.setPressureUnit(any()) } just Runs
        coEvery { preferencesManager.setWeatherApiKey(any()) } just Runs
        coEvery { preferencesManager.setBiometricEnabled(any()) } just Runs
        coEvery { preferencesManager.setAutoLockTimeout(any()) } just Runs

        viewModel = SettingsViewModel(preferencesManager)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects default preferences`() {
        // Then
        val state = viewModel.uiState.value
        assertEquals(Theme.SYSTEM, state.theme)
        assertEquals(TemperatureUnit.FAHRENHEIT, state.temperatureUnit)
        assertEquals(WindSpeedUnit.MPH, state.windSpeedUnit)
        assertEquals(PressureUnit.INHG, state.pressureUnit)
        assertNull(state.customApiKey)
        assertFalse(state.biometricEnabled)
        assertEquals(5, state.autoLockTimeout)
        assertFalse(state.showApiKeyDialog)
        assertFalse(state.showAutoLockDialog)
    }

    @Test
    fun `setTheme updates theme preference`() = runTest {
        // When
        viewModel.setTheme(Theme.DARK)
        advanceUntilIdle()

        // Then
        coVerify { preferencesManager.setTheme(Theme.DARK) }
    }

    @Test
    fun `theme change updates UI state`() = runTest {
        // When
        themeFlow.value = Theme.LIGHT
        advanceUntilIdle()

        // Then
        assertEquals(Theme.LIGHT, viewModel.uiState.value.theme)
    }

    @Test
    fun `setTemperatureUnit updates temperature preference`() = runTest {
        // When
        viewModel.setTemperatureUnit(TemperatureUnit.CELSIUS)
        advanceUntilIdle()

        // Then
        coVerify { preferencesManager.setTemperatureUnit(TemperatureUnit.CELSIUS) }
    }

    @Test
    fun `temperature unit change updates UI state`() = runTest {
        // When
        temperatureUnitFlow.value = TemperatureUnit.CELSIUS
        advanceUntilIdle()

        // Then
        assertEquals(TemperatureUnit.CELSIUS, viewModel.uiState.value.temperatureUnit)
    }

    @Test
    fun `setWindSpeedUnit updates wind speed preference`() = runTest {
        // When
        viewModel.setWindSpeedUnit(WindSpeedUnit.KPH)
        advanceUntilIdle()

        // Then
        coVerify { preferencesManager.setWindSpeedUnit(WindSpeedUnit.KPH) }
    }

    @Test
    fun `wind speed unit change updates UI state`() = runTest {
        // When
        windSpeedUnitFlow.value = WindSpeedUnit.MS
        advanceUntilIdle()

        // Then
        assertEquals(WindSpeedUnit.MS, viewModel.uiState.value.windSpeedUnit)
    }

    @Test
    fun `setPressureUnit updates pressure preference`() = runTest {
        // When
        viewModel.setPressureUnit(PressureUnit.HPA)
        advanceUntilIdle()

        // Then
        coVerify { preferencesManager.setPressureUnit(PressureUnit.HPA) }
    }

    @Test
    fun `setCustomApiKey updates API key preference`() = runTest {
        // Given
        val apiKey = "custom_api_key_123"

        // When
        viewModel.setCustomApiKey(apiKey)
        advanceUntilIdle()

        // Then
        coVerify { preferencesManager.setWeatherApiKey(apiKey) }
    }

    @Test
    fun `setCustomApiKey with null clears API key`() = runTest {
        // When
        viewModel.setCustomApiKey(null)
        advanceUntilIdle()

        // Then
        coVerify { preferencesManager.setWeatherApiKey(null) }
    }

    @Test
    fun `API key change updates UI state`() = runTest {
        // When
        apiKeyFlow.value = "new_api_key"
        advanceUntilIdle()

        // Then
        assertEquals("new_api_key", viewModel.uiState.value.customApiKey)
    }

    @Test
    fun `setBiometricEnabled updates biometric preference`() = runTest {
        // When
        viewModel.setBiometricEnabled(true)
        advanceUntilIdle()

        // Then
        coVerify { preferencesManager.setBiometricEnabled(true) }
    }

    @Test
    fun `biometric enabled change updates UI state`() = runTest {
        // When
        biometricEnabledFlow.value = true
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.biometricEnabled)
    }

    @Test
    fun `setAutoLockTimeout updates timeout preference`() = runTest {
        // When
        viewModel.setAutoLockTimeout(10)
        advanceUntilIdle()

        // Then
        coVerify { preferencesManager.setAutoLockTimeout(10) }
    }

    @Test
    fun `auto lock timeout change updates UI state`() = runTest {
        // When
        autoLockTimeoutFlow.value = 15
        advanceUntilIdle()

        // Then
        assertEquals(15, viewModel.uiState.value.autoLockTimeout)
    }

    @Test
    fun `showApiKeyDialog updates dialog state`() {
        // When
        viewModel.showApiKeyDialog()

        // Then
        assertTrue(viewModel.uiState.value.showApiKeyDialog)
    }

    @Test
    fun `hideApiKeyDialog updates dialog state`() {
        // Given
        viewModel.showApiKeyDialog()

        // When
        viewModel.hideApiKeyDialog()

        // Then
        assertFalse(viewModel.uiState.value.showApiKeyDialog)
    }

    @Test
    fun `showAutoLockDialog updates dialog state`() {
        // When
        viewModel.showAutoLockDialog()

        // Then
        assertTrue(viewModel.uiState.value.showAutoLockDialog)
    }

    @Test
    fun `hideAutoLockDialog updates dialog state`() {
        // Given
        viewModel.showAutoLockDialog()

        // When
        viewModel.hideAutoLockDialog()

        // Then
        assertFalse(viewModel.uiState.value.showAutoLockDialog)
    }

    @Test
    fun `multiple preference changes update UI state correctly`() = runTest {
        // When
        themeFlow.value = Theme.DARK
        temperatureUnitFlow.value = TemperatureUnit.CELSIUS
        windSpeedUnitFlow.value = WindSpeedUnit.KPH
        pressureUnitFlow.value = PressureUnit.HPA
        apiKeyFlow.value = "custom_key"
        biometricEnabledFlow.value = true
        autoLockTimeoutFlow.value = 20
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(Theme.DARK, state.theme)
        assertEquals(TemperatureUnit.CELSIUS, state.temperatureUnit)
        assertEquals(WindSpeedUnit.KPH, state.windSpeedUnit)
        assertEquals(PressureUnit.HPA, state.pressureUnit)
        assertEquals("custom_key", state.customApiKey)
        assertTrue(state.biometricEnabled)
        assertEquals(20, state.autoLockTimeout)
    }

    @Test
    fun `dialog states are independent`() {
        // When
        viewModel.showApiKeyDialog()

        // Then
        assertTrue(viewModel.uiState.value.showApiKeyDialog)
        assertFalse(viewModel.uiState.value.showAutoLockDialog)

        // When
        viewModel.showAutoLockDialog()

        // Then
        assertTrue(viewModel.uiState.value.showApiKeyDialog)
        assertTrue(viewModel.uiState.value.showAutoLockDialog)

        // When
        viewModel.hideApiKeyDialog()

        // Then
        assertFalse(viewModel.uiState.value.showApiKeyDialog)
        assertTrue(viewModel.uiState.value.showAutoLockDialog)
    }
}
