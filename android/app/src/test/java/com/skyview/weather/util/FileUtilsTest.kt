package com.skyview.weather.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for FileUtils.
 *
 * Tests file size formatting, MIME type detection, and file validation.
 */
class FileUtilsTest {

    @Test
    fun `formatFileSize handles zero bytes`() {
        assertEquals("0 B", FileUtils.formatFileSize(0))
    }

    @Test
    fun `formatFileSize formats bytes correctly`() {
        assertEquals("512 B", FileUtils.formatFileSize(512))
        assertEquals("1,023 B", FileUtils.formatFileSize(1023))
    }

    @Test
    fun `formatFileSize formats kilobytes correctly`() {
        assertEquals("1 KB", FileUtils.formatFileSize(1024))
        assertEquals("5.5 KB", FileUtils.formatFileSize(5632)) // 5.5 * 1024
        assertEquals("10 KB", FileUtils.formatFileSize(10_240))
    }

    @Test
    fun `formatFileSize formats megabytes correctly`() {
        assertEquals("1 MB", FileUtils.formatFileSize(1_048_576)) // 1024^2
        assertEquals("5.5 MB", FileUtils.formatFileSize(5_767_168)) // 5.5 * 1024^2
        assertEquals("100 MB", FileUtils.formatFileSize(104_857_600))
    }

    @Test
    fun `formatFileSize formats gigabytes correctly`() {
        assertEquals("1 GB", FileUtils.formatFileSize(1_073_741_824)) // 1024^3
        assertEquals("2.5 GB", FileUtils.formatFileSize(2_684_354_560L)) // 2.5 * 1024^3
    }

    @Test
    fun `formatFileSize formats terabytes correctly`() {
        assertEquals("1 TB", FileUtils.formatFileSize(1_099_511_627_776L)) // 1024^4
    }

    @Test
    fun `getMimeType returns correct MIME for images`() {
        assertEquals("image/jpeg", FileUtils.getMimeType("photo.jpg"))
        assertEquals("image/jpeg", FileUtils.getMimeType("image.jpeg"))
        assertEquals("image/png", FileUtils.getMimeType("screenshot.png"))
        assertEquals("image/gif", FileUtils.getMimeType("animation.gif"))
        assertEquals("image/webp", FileUtils.getMimeType("modern.webp"))
    }

    @Test
    fun `getMimeType returns correct MIME for videos`() {
        assertEquals("video/mp4", FileUtils.getMimeType("movie.mp4"))
        assertEquals("video/quicktime", FileUtils.getMimeType("clip.mov"))
        assertEquals("video/x-msvideo", FileUtils.getMimeType("old_video.avi"))
    }

    @Test
    fun `getMimeType returns correct MIME for audio`() {
        assertEquals("audio/mpeg", FileUtils.getMimeType("song.mp3"))
        assertEquals("audio/wav", FileUtils.getMimeType("recording.wav"))
        assertEquals("audio/ogg", FileUtils.getMimeType("track.ogg"))
    }

    @Test
    fun `getMimeType returns correct MIME for documents`() {
        assertEquals("application/pdf", FileUtils.getMimeType("document.pdf"))
        assertEquals("application/msword", FileUtils.getMimeType("old_doc.doc"))
        assertEquals("text/plain", FileUtils.getMimeType("readme.txt"))
        assertEquals("text/csv", FileUtils.getMimeType("data.csv"))
    }

    @Test
    fun `getMimeType handles unknown extensions`() {
        assertEquals("application/octet-stream", FileUtils.getMimeType("file.unknown"))
        assertEquals("application/octet-stream", FileUtils.getMimeType("noextension"))
    }

    @Test
    fun `getMimeType is case insensitive`() {
        assertEquals("image/jpeg", FileUtils.getMimeType("photo.JPG"))
        assertEquals("image/png", FileUtils.getMimeType("Image.PNG"))
        assertEquals("application/pdf", FileUtils.getMimeType("DOC.PDF"))
    }

    @Test
    fun `isImageFile detects image files correctly`() {
        assertTrue(FileUtils.isImageFile("photo.jpg"))
        assertTrue(FileUtils.isImageFile("image.png"))
        assertTrue(FileUtils.isImageFile("animation.gif"))
        assertTrue(FileUtils.isImageFile("PHOTO.JPG")) // Case insensitive

        assertFalse(FileUtils.isImageFile("video.mp4"))
        assertFalse(FileUtils.isImageFile("document.pdf"))
        assertFalse(FileUtils.isImageFile("song.mp3"))
    }

    @Test
    fun `isVideoFile detects video files correctly`() {
        assertTrue(FileUtils.isVideoFile("movie.mp4"))
        assertTrue(FileUtils.isVideoFile("clip.mov"))
        assertTrue(FileUtils.isVideoFile("video.avi"))

        assertFalse(FileUtils.isVideoFile("photo.jpg"))
        assertFalse(FileUtils.isVideoFile("document.pdf"))
    }

    @Test
    fun `isAudioFile detects audio files correctly`() {
        assertTrue(FileUtils.isAudioFile("song.mp3"))
        assertTrue(FileUtils.isAudioFile("recording.wav"))
        assertTrue(FileUtils.isAudioFile("track.ogg"))

        assertFalse(FileUtils.isAudioFile("video.mp4"))
        assertFalse(FileUtils.isAudioFile("photo.jpg"))
    }

    @Test
    fun `isDocumentFile detects document files correctly`() {
        assertTrue(FileUtils.isDocumentFile("report.pdf"))
        assertTrue(FileUtils.isDocumentFile("letter.doc"))
        assertTrue(FileUtils.isDocumentFile("data.xlsx"))
        assertTrue(FileUtils.isDocumentFile("notes.txt"))

        assertFalse(FileUtils.isDocumentFile("video.mp4"))
        assertFalse(FileUtils.isDocumentFile("photo.jpg"))
    }

    @Test
    fun `getSafeFileName removes special characters`() {
        assertEquals("safe_file.txt", FileUtils.getSafeFileName("safe file.txt"))
        assertEquals("file_with_____chars.pdf", FileUtils.getSafeFileName("file with @#$% chars.pdf"))
        assertEquals("normal.jpg", FileUtils.getSafeFileName("normal.jpg"))
    }

    @Test
    fun `getFileExtension extracts extension correctly`() {
        assertEquals("jpg", FileUtils.getFileExtension("photo.jpg"))
        assertEquals("pdf", FileUtils.getFileExtension("document.pdf"))
        assertEquals("txt", FileUtils.getFileExtension("file.name.with.dots.txt"))
        assertEquals("", FileUtils.getFileExtension("noextension"))
    }

    @Test
    fun `getFileExtension is case insensitive`() {
        assertEquals("jpg", FileUtils.getFileExtension("PHOTO.JPG"))
        assertEquals("pdf", FileUtils.getFileExtension("Document.PDF"))
    }

    @Test
    fun `getFileNameWithoutExtension removes extension`() {
        assertEquals("photo", FileUtils.getFileNameWithoutExtension("photo.jpg"))
        assertEquals("document", FileUtils.getFileNameWithoutExtension("document.pdf"))
        assertEquals("file.name.with.dots", FileUtils.getFileNameWithoutExtension("file.name.with.dots.txt"))
        assertEquals("noextension", FileUtils.getFileNameWithoutExtension("noextension"))
    }

    @Test
    fun `isFileNameSafe detects path traversal attempts`() {
        assertFalse(FileUtils.isFileNameSafe("../../../etc/passwd"))
        assertFalse(FileUtils.isFileNameSafe("..\\..\\windows\\system32"))
        assertFalse(FileUtils.isFileNameSafe("folder/file.txt"))
        assertFalse(FileUtils.isFileNameSafe("folder\\file.txt"))
    }

    @Test
    fun `isFileNameSafe detects empty names`() {
        assertFalse(FileUtils.isFileNameSafe(""))
        assertFalse(FileUtils.isFileNameSafe("   "))
        assertFalse(FileUtils.isFileNameSafe("\t"))
    }

    @Test
    fun `isFileNameSafe detects Windows reserved names`() {
        assertFalse(FileUtils.isFileNameSafe("CON"))
        assertFalse(FileUtils.isFileNameSafe("PRN"))
        assertFalse(FileUtils.isFileNameSafe("AUX"))
        assertFalse(FileUtils.isFileNameSafe("NUL"))
        assertFalse(FileUtils.isFileNameSafe("COM1"))
        assertFalse(FileUtils.isFileNameSafe("LPT1"))
        assertFalse(FileUtils.isFileNameSafe("con.txt")) // With extension
    }

    @Test
    fun `isFileNameSafe allows valid names`() {
        assertTrue(FileUtils.isFileNameSafe("safe_file.txt"))
        assertTrue(FileUtils.isFileNameSafe("photo.jpg"))
        assertTrue(FileUtils.isFileNameSafe("my-document-2024.pdf"))
        assertTrue(FileUtils.isFileNameSafe("file_with_underscores.doc"))
        assertTrue(FileUtils.isFileNameSafe("simple"))
    }

    @Test
    fun `isFileNameSafe allows safe special characters`() {
        assertTrue(FileUtils.isFileNameSafe("file-name.txt"))
        assertTrue(FileUtils.isFileNameSafe("file_name.txt"))
        assertTrue(FileUtils.isFileNameSafe("file.name.txt"))
    }
}
