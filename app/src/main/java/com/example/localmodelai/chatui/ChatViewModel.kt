package com.example.localmodelai.chatui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val llm = LocalLLMManager(application)
    private val downloader = ModelDownloader(application)
    private val activeModel = ModelCatalog.defaultModel
    private var downloadPollingJob: Job? = null

    val messages = mutableStateListOf(
        Message(
            "This app now downloads your hosted .task model on demand. Tap the model menu to download it, load it, and then chat locally on device.",
            false
        )
    )

    var selectedModel by mutableStateOf(activeModel.displayName)
        private set

    var isModelLoading by mutableStateOf(false)
        private set

    var isTyping by mutableStateOf(false)
        private set

    var isModelLoaded by mutableStateOf(false)
        private set

    var modelDownloadStatus by mutableStateOf(downloader.getDownloadStatus(activeModel))
        private set

    init {
        refreshModelStatus()
    }

    fun refreshModelStatus() {
        modelDownloadStatus = downloader.getDownloadStatus(activeModel)
        if (modelDownloadStatus.isDownloading) {
            startDownloadPolling()
        }
    }

    fun downloadSelectedModel() {
        downloader.downloadModel(activeModel)
        refreshModelStatus()
        startDownloadPolling()
    }

    fun loadSelectedModel() {
        if (!modelDownloadStatus.isDownloaded) {
            messages.add(
                Message("Download ${activeModel.displayName} first, then load it.", false)
            )
            return
        }

        isModelLoading = true
        viewModelScope.launch {
            try {
                val modelPath = downloader.getModelPath(activeModel)
                withContext(Dispatchers.IO) {
                    llm.loadModel(modelPath)
                }
                isModelLoaded = true
                selectedModel = activeModel.displayName
                messages.add(
                    Message("${activeModel.displayName} is loaded and ready for local chat.", false)
                )
            } catch (e: Exception) {
                isModelLoaded = false
                messages.add(
                    Message("Failed to load ${activeModel.displayName}: ${e.message ?: "unknown error"}", false)
                )
            } finally {
                isModelLoading = false
            }
        }
    }

    fun sendMessage(prompt: String) {
        messages.add(Message(prompt, true))

        viewModelScope.launch {
            if (!ensureModelLoaded()) {
                messages.add(
                    Message("The model is not ready yet. Download and load ${activeModel.displayName} from the model menu first.", false)
                )
                return@launch
            }

            isTyping = true
            val reply = withContext(Dispatchers.IO) {
                llm.generate(prompt)
            }
            messages.add(Message(reply, false))
            isTyping = false
        }
    }

    private suspend fun ensureModelLoaded(): Boolean {
        if (isModelLoaded) return true
        if (!modelDownloadStatus.isDownloaded) return false

        return try {
            withContext(Dispatchers.IO) {
                llm.loadModel(downloader.getModelPath(activeModel))
            }
            isModelLoaded = true
            true
        } catch (_: Exception) {
            isModelLoaded = false
            false
        }
    }

    private fun startDownloadPolling() {
        if (downloadPollingJob?.isActive == true) return

        downloadPollingJob = viewModelScope.launch {
            do {
                modelDownloadStatus = downloader.getDownloadStatus(activeModel)
                if (modelDownloadStatus.isDownloading) {
                    delay(1000)
                }
            } while (modelDownloadStatus.isDownloading)
        }
    }
}
