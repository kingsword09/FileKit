package io.github.vinceglb.picker.core.platform.awt

import io.github.vinceglb.picker.core.platform.PlatformFilePicker
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter
import kotlin.coroutines.resume

internal class AwtFilePicker : PlatformFilePicker {
    override suspend fun pickFile(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?
    ): File? = callAwtPicker(
        title = title,
        isMultipleMode = false,
        fileExtensions = fileExtensions,
        initialDirectory = initialDirectory
    )?.firstOrNull()

    override suspend fun pickFiles(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?
    ): List<File>? = callAwtPicker(
        title = title,
        isMultipleMode = true,
        fileExtensions = fileExtensions,
        initialDirectory = initialDirectory
    )

    override fun pickDirectory(initialDirectory: String?, title: String?): File? {
        throw UnsupportedOperationException("Directory picker is not supported on Linux yet.")
    }

    private suspend fun callAwtPicker(
        title: String?,
        isMultipleMode: Boolean,
        initialDirectory: String?,
        fileExtensions: List<String>?,
    ): List<File>? = suspendCancellableCoroutine { continuation ->
        val parent: Frame? = null
        val dialog = object : FileDialog(parent, title, LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                val result = files?.toList()
                continuation.resume(result)
            }
        }

        // Set multiple mode
        dialog.isMultipleMode = isMultipleMode

        // Set mime types
        dialog.filenameFilter = FilenameFilter { _, name ->
            fileExtensions?.any { name.endsWith(it) } ?: true
        }

        // Set initial directory
        dialog.directory = initialDirectory

        // Show the dialog
        dialog.isVisible = true

        // Dispose the dialog when the continuation is cancelled
        continuation.invokeOnCancellation { dialog.dispose() }
    }
}