package com.skyview.weather.core.security

import com.skyview.weather.util.TapTarget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TapSequenceTracker.
 *
 * Tests tap sequence recording, matching, and timeout behavior.
 */
class TapSequenceTrackerTest {

    private lateinit var tracker: TapSequenceTracker

    @Before
    fun setup() {
        tracker = TapSequenceTracker()
    }

    @Test
    fun `recordTap adds tap to buffer`() = runTest {
        // When
        tracker.recordTap(TapTarget.CLOUD_ICON)

        // Then
        val buffer = tracker.tapBuffer.first()
        assertEquals(1, buffer.size)
        assertEquals(TapTarget.CLOUD_ICON, buffer[0])
    }

    @Test
    fun `recording multiple taps maintains order`() = runTest {
        // When
        tracker.recordTap(TapTarget.CLOUD_ICON)
        tracker.recordTap(TapTarget.TEMPERATURE)
        tracker.recordTap(TapTarget.WIND_ICON)

        // Then
        val buffer = tracker.tapBuffer.first()
        assertEquals(3, buffer.size)
        assertEquals(TapTarget.CLOUD_ICON, buffer[0])
        assertEquals(TapTarget.TEMPERATURE, buffer[1])
        assertEquals(TapTarget.WIND_ICON, buffer[2])
    }

    @Test
    fun `setTargetSequence updates stored sequence`() = runTest {
        // Given
        val sequence = listOf(TapTarget.CLOUD_ICON, TapTarget.TEMPERATURE)

        // When
        tracker.setTargetSequence(sequence)

        // Then
        val storedSequence = tracker.targetSequence.first()
        assertEquals(sequence, storedSequence)
    }

    @Test
    fun `correct sequence triggers success`() = runTest {
        // Given
        val correctSequence = listOf(
            TapTarget.CLOUD_ICON,
            TapTarget.TEMPERATURE,
            TapTarget.HIGH_LOW_TEMP
        )
        tracker.setTargetSequence(correctSequence)

        // When
        var matchResult: TapSequenceResult? = null
        tracker.sequenceMatches.collect { result ->
            if (result != null) {
                matchResult = result
                return@collect
            }
        }

        // Simulate tap sequence
        correctSequence.forEach { target ->
            tracker.recordTap(target)
        }

        // Then
        assertNotNull(matchResult)
        assertTrue(matchResult is TapSequenceResult.Success)
    }

    @Test
    fun `incorrect sequence triggers failure`() = runTest {
        // Given
        val correctSequence = listOf(TapTarget.CLOUD_ICON, TapTarget.TEMPERATURE)
        val wrongSequence = listOf(TapTarget.CLOUD_ICON, TapTarget.WIND_ICON)
        tracker.setTargetSequence(correctSequence)

        // When
        var matchResult: TapSequenceResult? = null
        tracker.sequenceMatches.collect { result ->
            if (result != null) {
                matchResult = result
                return@collect
            }
        }

        wrongSequence.forEach { target ->
            tracker.recordTap(target)
        }

        // Then
        assertNotNull(matchResult)
        assertTrue(matchResult is TapSequenceResult.Failure)
    }

    @Test
    fun `clearBuffer removes all taps`() = runTest {
        // Given
        tracker.recordTap(TapTarget.CLOUD_ICON)
        tracker.recordTap(TapTarget.TEMPERATURE)

        // When
        tracker.clearBuffer()

        // Then
        val buffer = tracker.tapBuffer.first()
        assertEquals(0, buffer.size)
    }

    @Test
    fun `tap buffer limits size`() = runTest {
        // When - record more taps than buffer size
        repeat(15) {
            tracker.recordTap(TapTarget.CLOUD_ICON)
        }

        // Then - buffer should be limited to max size (10)
        val buffer = tracker.tapBuffer.first()
        assertTrue(buffer.size <= 10)
    }
}
