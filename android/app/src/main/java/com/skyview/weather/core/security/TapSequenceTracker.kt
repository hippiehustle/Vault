package com.skyview.weather.core.security

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skyview.weather.util.Constants
import com.skyview.weather.util.TapTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks widget tap sequences for vault authentication.
 *
 * Maintains a buffer of recent taps and checks if they match any configured
 * unlock sequences. Implements timing constraints and rate limiting.
 *
 * @property context Application context
 */
@Singleton
class TapSequenceTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val PREFS_NAME = "tap_sequence_prefs"
        private const val KEY_SEQUENCES = "configured_sequences"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LAST_ATTEMPT_TIME = "last_attempt_time"
    }

    /**
     * Tap sequence configuration.
     *
     * @property id Unique identifier
     * @property targets List of tap targets in order
     * @property vaultSection Section to unlock (for future multi-vault support)
     * @property name User-friendly name
     */
    data class TapSequence(
        val id: String,
        val targets: List<TapTarget>,
        val vaultSection: String = "main",
        val name: String = "Default"
    )

    /**
     * Recorded tap with timestamp.
     */
    private data class TapRecord(
        val target: TapTarget,
        val timestamp: Long
    )

    /**
     * Sequence match result.
     */
    sealed class SequenceMatchResult {
        data class Matched(val sequence: TapSequence) : SequenceMatchResult()
        object NoMatch : SequenceMatchResult()
        object RateLimited : SequenceMatchResult()
        object TimedOut : SequenceMatchResult()
    }

    private val tapBuffer = ConcurrentLinkedQueue<TapRecord>()
    private val gson = Gson()
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _matchResult = MutableStateFlow<SequenceMatchResult?>(null)
    val matchResult: StateFlow<SequenceMatchResult?> = _matchResult

    /**
     * Records a tap from the widget.
     *
     * @param target The tapped target
     * @return SequenceMatchResult if a sequence was matched or rate limit hit
     */
    fun recordTap(target: TapTarget): SequenceMatchResult {
        // Check rate limiting
        if (isRateLimited()) {
            return SequenceMatchResult.RateLimited
        }

        // Add tap to buffer
        val tapRecord = TapRecord(target, System.currentTimeMillis())
        tapBuffer.add(tapRecord)

        // Keep buffer size limited
        while (tapBuffer.size > Constants.TAP_BUFFER_SIZE) {
            tapBuffer.poll()
        }

        // Remove old taps (older than timeout)
        val cutoffTime = System.currentTimeMillis() - Constants.TAP_SEQUENCE_TIMEOUT_MS
        tapBuffer.removeAll { it.timestamp < cutoffTime }

        // Check for sequence match
        val result = checkSequences()
        _matchResult.value = result

        if (result is SequenceMatchResult.NoMatch) {
            // Increment failed attempts
            incrementFailedAttempts()
        } else if (result is SequenceMatchResult.Matched) {
            // Reset failed attempts on successful match
            resetFailedAttempts()
        }

        return result
    }

    /**
     * Checks if current tap buffer matches any configured sequence.
     *
     * @return SequenceMatchResult
     */
    private fun checkSequences(): SequenceMatchResult {
        val sequences = getConfiguredSequences()

        for (sequence in sequences) {
            if (matchesSequence(sequence)) {
                return SequenceMatchResult.Matched(sequence)
            }
        }

        return SequenceMatchResult.NoMatch
    }

    /**
     * Checks if tap buffer matches a specific sequence.
     *
     * @param sequence The sequence to check
     * @return true if matched
     */
    private fun matchesSequence(sequence: TapSequence): Boolean {
        val bufferList = tapBuffer.toList()

        if (bufferList.size < sequence.targets.size) {
            return false
        }

        // Get the most recent taps matching sequence length
        val recentTaps = bufferList.takeLast(sequence.targets.size)

        // Check if targets match in order
        val targetsMatch = recentTaps.map { it.target } == sequence.targets

        if (!targetsMatch) {
            return false
        }

        // Check timing constraint (must complete within timeout)
        val firstTapTime = recentTaps.first().timestamp
        val lastTapTime = recentTaps.last().timestamp
        val timeSpan = lastTapTime - firstTapTime

        return timeSpan <= Constants.TAP_SEQUENCE_TIMEOUT_MS
    }

    /**
     * Gets all configured tap sequences.
     *
     * @return List of tap sequences
     */
    fun getConfiguredSequences(): List<TapSequence> {
        val json = prefs.getString(KEY_SEQUENCES, null) ?: return getDefaultSequences()

        return try {
            val type = object : TypeToken<List<TapSequence>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            getDefaultSequences()
        }
    }

    /**
     * Gets default tap sequences.
     *
     * @return List of default sequences
     */
    private fun getDefaultSequences(): List<TapSequence> {
        return listOf(
            TapSequence(
                id = "default",
                targets = listOf(
                    TapTarget.CLOUD_ICON,
                    TapTarget.TEMPERATURE,
                    TapTarget.HOURLY_CHART
                ),
                vaultSection = "main",
                name = "Default Sequence"
            )
        )
    }

    /**
     * Saves tap sequences to preferences.
     *
     * @param sequences List of sequences to save
     */
    fun saveSequences(sequences: List<TapSequence>) {
        val json = gson.toJson(sequences)
        prefs.edit()
            .putString(KEY_SEQUENCES, json)
            .apply()
    }

    /**
     * Adds a new tap sequence.
     *
     * @param sequence Sequence to add
     */
    fun addSequence(sequence: TapSequence) {
        val current = getConfiguredSequences().toMutableList()
        current.add(sequence)
        saveSequences(current)
    }

    /**
     * Removes a tap sequence.
     *
     * @param sequenceId ID of sequence to remove
     */
    fun removeSequence(sequenceId: String) {
        val current = getConfiguredSequences().toMutableList()
        current.removeAll { it.id == sequenceId }
        saveSequences(current)
    }

    /**
     * Clears the tap buffer.
     */
    fun clearBuffer() {
        tapBuffer.clear()
        _matchResult.value = null
    }

    /**
     * Checks if tap sequence attempts are rate limited.
     *
     * @return true if rate limited
     */
    private fun isRateLimited(): Boolean {
        val failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
        val lastAttemptTime = prefs.getLong(KEY_LAST_ATTEMPT_TIME, 0)

        if (failedAttempts >= Constants.MAX_FAILED_SEQUENCE_ATTEMPTS) {
            val timeSinceLastAttempt = System.currentTimeMillis() - lastAttemptTime

            // Rate limit for 1 hour after max failed attempts
            if (timeSinceLastAttempt < Constants.SEQUENCE_ATTEMPT_WINDOW_MS) {
                return true
            } else {
                // Reset after time window
                resetFailedAttempts()
            }
        }

        return false
    }

    /**
     * Increments failed attempt counter.
     */
    private fun incrementFailedAttempts() {
        val current = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, current + 1)
            .putLong(KEY_LAST_ATTEMPT_TIME, System.currentTimeMillis())
            .apply()
    }

    /**
     * Resets failed attempt counter.
     */
    private fun resetFailedAttempts() {
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LAST_ATTEMPT_TIME, 0)
            .apply()
    }

    /**
     * Gets remaining time for rate limit (in milliseconds).
     *
     * @return Remaining time or 0 if not rate limited
     */
    fun getRateLimitRemainingTime(): Long {
        val lastAttemptTime = prefs.getLong(KEY_LAST_ATTEMPT_TIME, 0)
        val timeSinceLastAttempt = System.currentTimeMillis() - lastAttemptTime
        val remaining = Constants.SEQUENCE_ATTEMPT_WINDOW_MS - timeSinceLastAttempt

        return maxOf(0, remaining)
    }
}
