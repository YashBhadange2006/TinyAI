package com.example.localmodelai.screens.chat

import androidx.lifecycle.viewModelScope
import com.example.localmodelai.ai.ModelCatalog
import com.example.localmodelai.ai.ModelDownloadStatus
import com.example.localmodelai.ai.ModelSpec
import com.example.localmodelai.data.api.HFRemoteModelGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun ChatViewModel.refreshModelStatus() {
    refreshModelStatus(activeModel)
}

fun ChatViewModel.refreshModelStatus(model: ModelSpec) {
    val latestStatus = downloader.getDownloadStatus(model)
    modelDownloadStatus = if (
        downloadingModelId == model.id &&
        !latestStatus.isDownloaded &&
        !latestStatus.statusMessage.contains("failed", ignoreCase = true)
    ) {
        latestStatus.copy(
            isDownloading = true,
            progressPercent = latestStatus.progressPercent ?: modelDownloadStatus.progressPercent ?: 0,
            statusMessage = if (latestStatus.statusMessage == "Not downloaded") {
                "Starting download"
            } else {
                latestStatus.statusMessage
            }
        )
    } else {
        latestStatus
    }
    isModelLoaded = loadedModelId == activeModel.id
    if (modelDownloadStatus.isDownloaded || modelDownloadStatus.statusMessage.contains("failed", ignoreCase = true)) {
        if (downloadingModelId == model.id) {
            downloadingModelId = null
        }
    }
    if (modelDownloadStatus.isDownloading || downloadingModelId == model.id) {
        startDownloadPolling(model)
    }
}

fun ChatViewModel.downloadSelectedModel(model: ModelSpec) {
    downloadingModelId = model.id
    modelDownloadStatus = ModelDownloadStatus(
        isDownloaded = false,
        isDownloading = true,
        progressPercent = 0,
        statusMessage = "Starting download"
    )
    downloader.downloadModel(model)
    refreshModelStatus(model)
    startDownloadPolling(model)
}

fun ChatViewModel.loadSelectedModel(model: ModelSpec) {
    val selectedPrompt = systemPromptDrafts[model.id] ?: if (model.id == activeModel.id) currentSystemPrompt else ""
    activeModel = model
    selectedModel = model.displayName
    currentSystemPrompt = selectedPrompt
    systemPromptDrafts[model.id] = currentSystemPrompt
    val status = downloader.getDownloadStatus(model)
    modelDownloadStatus = status

    if (!status.isDownloaded) {
        messages.add(
            Message("Download ${model.displayName} first, then load it.", false)
        )
        return
    }

    viewModelScope.launch {
        loadModelForContext(
            model = model,
            systemPrompt = currentSystemPrompt,
            confirmationMessage = "${model.displayName} is loaded and ready for local chat.",
            persistSessionConfig = currentSessionId != null
        )
    }
}

fun ChatViewModel.deleteSelectedModel(model: ModelSpec) {
    viewModelScope.launch {
        val deleted = withContext(Dispatchers.IO) {
            downloader.deleteModel(model)
        }

        if (deleted) {
            if (loadedModelId == model.id) {
                unloadActiveConversation()
            }
            if (loadingModelId == model.id) {
                loadingModelId = null
                isModelLoading = false
            }
            if (downloadingModelId == model.id) {
                downloadingModelId = null
            }
            if (activeModel.id == model.id) {
                modelDownloadStatus = downloader.getDownloadStatus(model)
            }
            messages.add(
                Message("${model.displayName} was deleted from device storage.", false)
            )
        } else {
            messages.add(
                Message("Failed to delete ${model.displayName} from device storage.", false)
            )
        }
    }
}

fun ChatViewModel.getModelStatus(model: ModelSpec): ModelDownloadStatus {
    return if (model.id == downloadingModelId) {
        modelDownloadStatus
    } else {
        downloader.getDownloadStatus(model)
    }
}

fun ChatViewModel.getRemoteModelGroup(repoId: String): HFRemoteModelGroup? {
    return remoteModelGroups.firstOrNull { it.id == repoId }
}

fun ChatViewModel.isLoadingModel(model: ModelSpec): Boolean = loadingModelId == model.id && isModelLoading

fun ChatViewModel.isLoadedModel(model: ModelSpec): Boolean {
    val isSameUnsavedChat = loadedSessionId == null && currentSessionId == null
    val isSameSavedChat = loadedSessionId != null && loadedSessionId == currentSessionId
    return loadedModelId == model.id && isModelLoaded && (isSameUnsavedChat || isSameSavedChat)
}

fun ChatViewModel.getSystemPrompt(model: ModelSpec): String {
    return if (model.id == activeModel.id) {
        currentSystemPrompt
    } else {
        systemPromptDrafts[model.id] ?: ""
    }
}

fun ChatViewModel.updateSystemPrompt(model: ModelSpec, prompt: String) {
    val trimmedPrompt = prompt
    systemPromptDrafts[model.id] = trimmedPrompt
    if (model.id == activeModel.id) {
        currentSystemPrompt = trimmedPrompt
        if (isModelLoaded) {
            unloadActiveConversation()
        }
    }

    currentSessionId?.let { sessionId ->
        if (model.id == activeModel.id) {
            viewModelScope.launch(Dispatchers.IO) {
                chatDao.updateSessionSystemPrompt(sessionId, trimmedPrompt)
            }
        }
    }
}

fun ChatViewModel.updateCurrentSystemPrompt(prompt: String) {
    updateSystemPrompt(activeModel, prompt)
}

fun ChatViewModel.reloadActiveModel() {
    loadSelectedModel(activeModel)
}

fun ChatViewModel.loadRemoteModelCatalog() {
    viewModelScope.launch {
        remoteModelGroups = withContext(Dispatchers.IO) {
            try {
                remoteModelsRepository.fetchRemoteLiteRtModels()
            } catch (_: Exception) {
                emptyList()
            }
        }
        restoreLoadedSessionModelIfNeeded()
    }
}

internal fun ChatViewModel.allKnownModels(): List<ModelSpec> {
    return ModelCatalog.supportedModels + remoteModelGroups.flatMap { it.toVersionModelSpecs() }
}

internal fun ChatViewModel.findKnownModelByDisplayName(displayName: String): ModelSpec? {
    return allKnownModels().firstOrNull { it.displayName == displayName }
}

internal fun ChatViewModel.unloadActiveConversation() {
    llm.close()
    loadedModelId = null
    loadedSessionId = null
    loadingModelId = null
    isModelLoaded = false
    isModelLoading = false
    modelLoadIndicator = null
}

internal suspend fun ChatViewModel.loadModelForContext(
    model: ModelSpec,
    systemPrompt: String,
    confirmationMessage: String,
    persistSessionConfig: Boolean
): Boolean {
    val normalizedPrompt = systemPrompt.trim()
    val status = downloader.getDownloadStatus(model)
    modelDownloadStatus = status

    if (!status.isDownloaded) {
        messages.add(
            Message("Download ${model.displayName} first, then load it.", false)
        )
        loadedModelId = null
        loadedSessionId = null
        isModelLoaded = false
        return false
    }

    val shouldReload = loadedModelId != model.id || loadedSessionId != currentSessionId || currentSystemPrompt != normalizedPrompt || !isModelLoaded
    if (shouldReload) {
        unloadActiveConversation()
    }

    isModelLoading = true
    loadingModelId = model.id
    return try {
        val modelPath = downloader.getModelPath(model)
        withContext(Dispatchers.IO) {
            llm.loadModel(modelPath, normalizedPrompt)
        }
        loadedModelId = model.id
        loadedSessionId = currentSessionId
        currentSystemPrompt = normalizedPrompt
        systemPromptDrafts[model.id] = normalizedPrompt
        isModelLoaded = true

        if (persistSessionConfig && currentSessionId != null) {
            withContext(Dispatchers.IO) {
                chatDao.updateSessionModelConfig(
                    sessionId = currentSessionId!!,
                    modelName = model.displayName,
                    systemPrompt = normalizedPrompt
                )
            }
        }

        messages.add(Message(confirmationMessage, false))
        true
    } catch (e: Exception) {
        loadedModelId = null
        loadedSessionId = null
        isModelLoaded = false
        messages.add(
            Message("Failed to load ${model.displayName}: ${e.message ?: "unknown error"}", false)
        )
        false
    } finally {
        isModelLoading = false
        loadingModelId = null
    }
}

private fun ChatViewModel.startDownloadPolling(model: ModelSpec) {
    downloadPollingJob?.cancel()

    downloadPollingJob = viewModelScope.launch {
        var attempts = 0
        while (attempts < 600) {
            val latestStatus = downloader.getDownloadStatus(model)
            isModelLoaded = loadedModelId == activeModel.id
            modelDownloadStatus = if (
                downloadingModelId == model.id &&
                !latestStatus.isDownloaded &&
                !latestStatus.statusMessage.contains("failed", ignoreCase = true)
            ) {
                latestStatus.copy(
                    isDownloading = true,
                    progressPercent = latestStatus.progressPercent ?: modelDownloadStatus.progressPercent ?: 0,
                    statusMessage = if (latestStatus.statusMessage == "Not downloaded") {
                        "Starting download"
                    } else {
                        latestStatus.statusMessage
                    }
                )
            } else {
                latestStatus
            }

            if (modelDownloadStatus.isDownloaded) {
                if (downloadingModelId == model.id) {
                    downloadingModelId = null
                }
                break
            }

            val isFailure = modelDownloadStatus.statusMessage.contains("failed", ignoreCase = true)
            if (isFailure) {
                if (downloadingModelId == model.id) {
                    downloadingModelId = null
                }
                break
            }

            attempts++
            delay(1000)
        }
    }
}

private fun ChatViewModel.restoreLoadedSessionModelIfNeeded() {
    val sessionId = currentSessionId ?: return
    if (loadedModelId != null) return

    viewModelScope.launch {
        val session = withContext(Dispatchers.IO) {
            chatDao.getSessionById(sessionId)
        } ?: return@launch

        val modelName = session.modelName.takeIf { it.isNotBlank() } ?: return@launch
        findKnownModelByDisplayName(modelName)?.let { model ->
            loadModelForContext(
                model = model,
                systemPrompt = session.systemPrompt,
                confirmationMessage = "Restored ${model.displayName} for this chat.",
                persistSessionConfig = false
            )
        }
    }
}
