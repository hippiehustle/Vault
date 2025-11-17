package com.skyview.weather.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper

/**
 * Clipboard utilities with automatic clearing for security.
 */
object ClipboardUtils {

    /**
     * Copies text to clipboard and automatically clears it after a delay.
     * Use for sensitive data like passwords.
     *
     * @param context Android context
     * @param label Label for the clip data
     * @param text Text to copy
     * @param clearDelayMs Delay before auto-clearing (default: 30 seconds)
     */
    fun copyWithAutoClear(
        context: Context,
        label: String,
        text: String,
        clearDelayMs: Long = Constants.CLIPBOARD_CLEAR_DELAY_MS
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)

        // Schedule automatic clearing
        Handler(Looper.getMainLooper()).postDelayed({
            clearClipboard(context)
        }, clearDelayMs)
    }

    /**
     * Clears the clipboard immediately.
     */
    fun clearClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("", "")
        clipboard.setPrimaryClip(clip)
    }
}
