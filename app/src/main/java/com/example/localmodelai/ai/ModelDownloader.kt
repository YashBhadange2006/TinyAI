package com.example.localmodelai.ai

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import java.io.File

data class ModelDownloadStatus(
    val isDownloaded: Boolean,
    val isDownloading: Boolean,
    val progressPercent: Int?,
    val statusMessage: String
)

class ModelDownloader(
    private val context: Context
) {
    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val preferences =
        context.getSharedPreferences("model_downloads", Context.MODE_PRIVATE)

    fun downloadModel(model: ModelSpec) {
        Log.d("ModelDownloader", "Initiating download for: ${model.displayName}")
        Log.d("ModelDownloader", "Download URL: ${model.downloadUrl}")

        if (isModelDownloaded(model)) return

        val currentStatus = getDownloadStatus(model)
        if (currentStatus.isDownloading) return

        val request = DownloadManager.Request(Uri.parse(model.downloadUrl)).apply {
            setTitle("Downloading ${model.displayName}")
            setDescription("Model download in progress")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverMetered(true)
            setDestinationInExternalFilesDir(context, "models", model.fileName)
        }

        val downloadId = downloadManager.enqueue(request)
        preferences.edit().putLong(downloadKey(model), downloadId).apply()
    }

    fun getDownloadStatus(model: ModelSpec): ModelDownloadStatus {
        val downloadId = preferences.getLong(downloadKey(model), -1L)
        val modelFile = getModelFile(model)
        if (downloadId == -1L) {
            if (modelFile.exists()) {
                return ModelDownloadStatus(
                    isDownloaded = true,
                    isDownloading = false,
                    progressPercent = 100,
                    statusMessage = "Downloaded"
                )
            }
            return ModelDownloadStatus(
                isDownloaded = false,
                isDownloading = false,
                progressPercent = null,
                statusMessage = "Not downloaded"
            )
        }

        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        return cursor.useDownloadRow { row ->
            if (row == null) {
                clearStoredDownload(model)
                return@useDownloadRow ModelDownloadStatus(
                    isDownloaded = false,
                    isDownloading = false,
                    progressPercent = null,
                    statusMessage = "Download not found. Try again."
                )
            }

            val status = row.getInt(row.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            val downloadedBytes =
                row.getLong(row.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val totalBytes =
                row.getLong(row.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            val progressPercent = if (downloadedBytes >= 0 && totalBytes > 0) {
                ((downloadedBytes * 100) / totalBytes).toInt()
            } else {
                null
            }

            when (status) {
                DownloadManager.STATUS_PENDING -> ModelDownloadStatus(
                    isDownloaded = false,
                    isDownloading = true,
                    progressPercent = progressPercent,
                    statusMessage = "Waiting to start download"
                )

                DownloadManager.STATUS_RUNNING -> ModelDownloadStatus(
                    isDownloaded = false,
                    isDownloading = true,
                    progressPercent = progressPercent,
                    statusMessage = progressPercent?.let { "Downloading $it%" } ?: "Downloading"
                )

                DownloadManager.STATUS_PAUSED -> ModelDownloadStatus(
                    isDownloaded = false,
                    isDownloading = true,
                    progressPercent = progressPercent,
                    statusMessage = "Download paused"
                )

                DownloadManager.STATUS_SUCCESSFUL -> {
                    clearStoredDownload(model)
                    ModelDownloadStatus(
                        isDownloaded = true,
                        isDownloading = false,
                        progressPercent = 100,
                        statusMessage = "Downloaded"
                    )
                }

                DownloadManager.STATUS_FAILED -> {
                    clearStoredDownload(model)
                    ModelDownloadStatus(
                        isDownloaded = false,
                        isDownloading = false,
                        progressPercent = progressPercent,
                        statusMessage = "Download failed. Tap download again."
                    )
                }

                else -> ModelDownloadStatus(
                    isDownloaded = false,
                    isDownloading = false,
                    progressPercent = progressPercent,
                    statusMessage = "Unknown download state"
                )
            }
        }
    }

    fun isModelDownloaded(model: ModelSpec): Boolean = getModelFile(model).exists()

    fun getModelPath(model: ModelSpec): String = getModelFile(model).absolutePath

    fun deleteModel(model: ModelSpec): Boolean {
        val downloadId = preferences.getLong(downloadKey(model), -1L)
        if (downloadId != -1L) {
            downloadManager.remove(downloadId)
        }

        clearStoredDownload(model)
        val modelFile = getModelFile(model)
        return !modelFile.exists() || modelFile.delete()
    }

    private fun getModelFile(model: ModelSpec): File {
        val file = File(context.getExternalFilesDir("models"), model.fileName)
        Log.d("MODEL_FILE", file.absolutePath)
        Log.d("MODEL_EXISTS", file.exists().toString())
        return file
    }


    private fun downloadKey(model: ModelSpec): String = "download_id_${model.id}"

    private fun clearStoredDownload(model: ModelSpec) {
        preferences.edit().remove(downloadKey(model)).apply()
    }
}

private inline fun <T> Cursor.useDownloadRow(block: (Cursor?) -> T): T {
    return use { cursor ->
        if (cursor == null || !cursor.moveToFirst()) {
            block(null)
        } else {
            block(cursor)
        }
    }
}
