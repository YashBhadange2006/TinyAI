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
    private var activeModel = ModelCatalog.defaultModel
    private var downloadPollingJob: Job? = null
    private var loadedModelId: String? = null

    val messages = mutableStateListOf(
        Message(
            "This app now downloads your hosted MediaPipe .task model on demand. Tap the model menu to download it, load it, and then chat locally on device.",
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

    fun refreshModelStatus(model: ModelSpec = activeModel) {
        activeModel = model
        modelDownloadStatus = downloader.getDownloadStatus(model)
        isModelLoaded = loadedModelId == model.id
        if (modelDownloadStatus.isDownloading) {
            startDownloadPolling(model)
        }
    }

    fun downloadSelectedModel(model : ModelSpec) {
        activeModel = model
        selectedModel = model.displayName
        downloader.downloadModel(model)
        refreshModelStatus(model)
        startDownloadPolling(model)
    }

    fun loadSelectedModel(model : ModelSpec) {
        activeModel = model
        selectedModel = model.displayName
        val status = downloader.getDownloadStatus(model)
        modelDownloadStatus = status

        if (!status.isDownloaded) {
            messages.add(
                Message("Download ${model.displayName} first, then load it.", false)
            )
            return
        }

        isModelLoading = true
        viewModelScope.launch {
            try {
                val modelPath = downloader.getModelPath(model)
                withContext(Dispatchers.IO) {
                    llm.loadModel(modelPath)
                }
                loadedModelId = model.id
                isModelLoaded = true
                messages.add(
                    Message("${model.displayName} is loaded and ready for local chat.", false)
                )
            } catch (e: Exception) {
                loadedModelId = null
                isModelLoaded = false
                messages.add(
                    Message("Failed to load ${model.displayName}: ${e.message ?: "unknown error"}", false)
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
        if (loadedModelId == activeModel.id && isModelLoaded) return true
        if (!modelDownloadStatus.isDownloaded) return false

        return try {
            withContext(Dispatchers.IO) {
                llm.loadModel(downloader.getModelPath(activeModel))
            }
            loadedModelId = activeModel.id
            isModelLoaded = true
            true
        } catch (_: Exception) {
            loadedModelId = null
            isModelLoaded = false
            false
        }
    }

    fun getModelStatus(model: ModelSpec): ModelDownloadStatus {
        return if (model.id == activeModel.id) {
            modelDownloadStatus
        } else {
            downloader.getDownloadStatus(model)
        }
    }

    fun isLoadedModel(model: ModelSpec): Boolean = loadedModelId == model.id

    private fun startDownloadPolling(model: ModelSpec) {
        downloadPollingJob?.cancel()

        downloadPollingJob = viewModelScope.launch {
            do {
                modelDownloadStatus = downloader.getDownloadStatus(model)
                activeModel = model
                isModelLoaded = loadedModelId == model.id
                if (modelDownloadStatus.isDownloading) {
                    delay(1000)
                }
            } while (modelDownloadStatus.isDownloading)
        }
    }
}
