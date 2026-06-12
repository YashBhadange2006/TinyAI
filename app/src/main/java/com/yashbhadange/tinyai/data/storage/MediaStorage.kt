package com.yashbhadange.tinyai.data.storage

import android.app.Application
import android.net.Uri
import java.io.File

class MediaStorage(private val application: Application) {

    fun saveChatImage(uri: Uri, imageName: String): String {
        val safeName = imageName
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "image_${System.currentTimeMillis()}" }
        val imageDir = getChatImagesDirectory()
        val targetFile = File(imageDir, "${System.currentTimeMillis()}_$safeName")
        application.contentResolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to read selected image")
        return targetFile.absolutePath
    }

    fun loadInternalMediaFiles(): List<File> {
        val imageDir = getChatImagesDirectory()
        if (!imageDir.exists()) return emptyList()

        return imageDir.listFiles()
            ?.filter { file ->
                file.isFile && IMAGE_EXTENSIONS.any { ext ->
                    file.name.endsWith(ext, ignoreCase = true)
                }
            }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    private fun getChatImagesDirectory(): File {
        return File(application.filesDir, CHAT_IMAGES_DIR).apply { mkdirs() }
    }

    private companion object {
        const val CHAT_IMAGES_DIR = "chat_images"
        val IMAGE_EXTENSIONS = listOf(".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp")
    }
}