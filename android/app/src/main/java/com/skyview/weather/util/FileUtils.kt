package com.skyview.weather.util

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

/**
 * File utility functions.
 *
 * Provides helpers for file operations, size formatting, and file type detection.
 */
object FileUtils {

    /**
     * Formats a byte size into a human-readable string.
     *
     * Examples:
     * - 1024 bytes → "1.0 KB"
     * - 1_048_576 bytes → "1.0 MB"
     * - 1_073_741_824 bytes → "1.0 GB"
     *
     * @param bytes Size in bytes
     * @return Formatted size string (e.g., "1.5 MB")
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()

        val size = bytes / 1024.0.pow(digitGroups.toDouble())
        val df = DecimalFormat("#,##0.#")

        return "${df.format(size)} ${units[digitGroups]}"
    }

    /**
     * Determines MIME type from file extension.
     *
     * @param fileName File name with extension
     * @return MIME type string (defaults to "application/octet-stream")
     */
    fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()

        return when (extension) {
            // Images
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "svg" -> "image/svg+xml"

            // Videos
            "mp4" -> "video/mp4"
            "mov" -> "video/quicktime"
            "avi" -> "video/x-msvideo"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"

            // Audio
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "m4a" -> "audio/mp4"
            "flac" -> "audio/flac"

            // Documents
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "txt" -> "text/plain"
            "csv" -> "text/csv"

            // Archives
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "7z" -> "application/x-7z-compressed"
            "tar" -> "application/x-tar"
            "gz" -> "application/gzip"

            // Default
            else -> "application/octet-stream"
        }
    }

    /**
     * Checks if a file extension represents an image.
     *
     * @param fileName File name with extension
     * @return True if the file is an image
     */
    fun isImageFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg")
    }

    /**
     * Checks if a file extension represents a video.
     *
     * @param fileName File name with extension
     * @return True if the file is a video
     */
    fun isVideoFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in setOf("mp4", "mov", "avi", "mkv", "webm", "m4v")
    }

    /**
     * Checks if a file extension represents an audio file.
     *
     * @param fileName File name with extension
     * @return True if the file is audio
     */
    fun isAudioFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in setOf("mp3", "wav", "ogg", "m4a", "flac", "aac")
    }

    /**
     * Checks if a file extension represents a document.
     *
     * @param fileName File name with extension
     * @return True if the file is a document
     */
    fun isDocumentFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv")
    }

    /**
     * Gets a safe file name by removing special characters.
     *
     * @param fileName Original file name
     * @return Safe file name with only alphanumeric characters, dots, and underscores
     */
    fun getSafeFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }

    /**
     * Extracts file extension from a file name.
     *
     * @param fileName File name
     * @return File extension (lowercase) or empty string if no extension
     */
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }

    /**
     * Gets file name without extension.
     *
     * @param fileName File name
     * @return File name without extension
     */
    fun getFileNameWithoutExtension(fileName: String): String {
        return fileName.substringBeforeLast('.')
    }

    /**
     * Validates that a file name is safe and doesn't contain path traversal attempts.
     *
     * @param fileName File name to validate
     * @return True if the file name is safe
     */
    fun isFileNameSafe(fileName: String): Boolean {
        // Check for path traversal attempts
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return false
        }

        // Check for empty or whitespace-only names
        if (fileName.isBlank()) {
            return false
        }

        // Check for reserved Windows file names
        val reserved = setOf("CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4",
                             "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2",
                             "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9")
        val nameWithoutExt = getFileNameWithoutExtension(fileName).uppercase()
        if (nameWithoutExt in reserved) {
            return false
        }

        return true
    }
}
