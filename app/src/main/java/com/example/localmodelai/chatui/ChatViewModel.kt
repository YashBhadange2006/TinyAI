package com.example.localmodelai.chatui

import android.app.Application
import android.net.Uri
import java.io.File
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.localmodelai.roomdb.AppDatabase
import com.example.localmodelai.roomdb.ChatMessageEntity
import com.example.localmodelai.roomdb.ChatSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private companion object {
        const val CONTEXT_MESSAGE_LIMIT = 4
        const val SESSION_TITLE_MAX_LENGTH = 40
    }

    private val llm = LocalLLMManager(application)
    private val downloader = ModelDownloader(application)
    private val chatDao = AppDatabase.getDatabase(application).chatDao()
    private var activeModel = ModelCatalog.defaultModel
    private var downloadPollingJob: Job? = null
    private var loadedModelId: String? = null
    private var downloadingModelId: String? = null
    private var loadingModelId: String? = null
    private var currentSessionId by mutableStateOf<Long?>(null)

    val messages = mutableStateListOf<Message>()

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

    var chatSessions by mutableStateOf<List<ChatSession>>(emptyList())
        private set

    var selectedAttachmentUri by mutableStateOf<Uri?>(null)
        private set

    var selectedAttachmentName by mutableStateOf<String?>(null)
        private set

    var selectedAttachmentMimeType by mutableStateOf<String?>(null)
        private set

    init {
        setIntroMessageIfNeeded()
        refreshModelStatus()
        loadChatSessions()
        restoreLatestSession()
    }

    fun refreshModelStatus(model: ModelSpec = activeModel) {
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

    fun downloadSelectedModel(model : ModelSpec) {
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
        loadingModelId = model.id
        viewModelScope.launch {
            try {
                if (loadedModelId != null && loadedModelId != model.id) {
                    llm.close()
                    loadedModelId = null
                    isModelLoaded = false
                }
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
                loadingModelId = null
            }
        }
    }

    fun deleteSelectedModel(model: ModelSpec) {
        viewModelScope.launch {
            val deleted = withContext(Dispatchers.IO) {
                downloader.deleteModel(model)
            }

            if (deleted) {
                if (loadedModelId == model.id) {
                    llm.close()
                    loadedModelId = null
                    isModelLoaded = false
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

    fun sendMessage(prompt: String) {
        if (selectedAttachmentUri != null) {
            val attachmentUri = selectedAttachmentUri
            val attachmentName = selectedAttachmentName ?: "Selected image"
            val attachmentMimeType = selectedAttachmentMimeType
            val effectivePrompt = prompt.ifBlank { "Describe this image." }
            val userMessage = effectivePrompt

            viewModelScope.launch {
                val savedImagePath = withContext(Dispatchers.IO) {
                    attachmentUri?.let { copyImageToAppStorage(it, attachmentName) }
                }

                messages.add(
                    Message(
                        text = userMessage,
                        isUser = true,
                        messageType = "image",
                        imagePath = savedImagePath,
                        imageName = attachmentName
                    )
                )

                if (!ensureModelLoaded()) {
                    messages.add(
                        Message("The model is not ready yet. Download and load ${activeModel.displayName} from the model menu first.", false)
                    )
                    return@launch
                }

                persistMessage(
                    text = userMessage,
                    isUser = true,
                    messageType = "image",
                    imagePath = savedImagePath,
                    imageName = attachmentName
                )
                isTyping = true
                val reply = withContext(Dispatchers.IO) {
                    when {
                        attachmentUri == null -> "No image was selected."
                        attachmentMimeType?.startsWith("image/") != true -> {
                            "Only image attachments are supported right now."
                        }
                        else -> {
                            llm.generateWithImage(effectivePrompt, attachmentUri)
                        }
                    }
                }
                messages.add(Message(reply, false))
                persistMessage(reply, isUser = false)
                clearSelectedAttachment()
                isTyping = false
            }
            return
        }

        messages.add(Message(prompt, true))

        viewModelScope.launch {
            if (!ensureModelLoaded()) {
                messages.add(
                    Message("The model is not ready yet. Download and load ${activeModel.displayName} from the model menu first.", false)
                )
                return@launch
            }

            persistMessage(prompt, isUser = true)
            isTyping = true
            val contextualPrompt = buildContextualPrompt()
            val reply = withContext(Dispatchers.IO) {
                llm.generate(contextualPrompt)
            }
            messages.add(Message(reply, false))
            persistMessage(reply, isUser = false)
            isTyping = false
        }
    }

    private fun buildContextualPrompt(): String {
        val recentMessages = messages
            .let { currentMessages ->
                if (
                    currentMessages.firstOrNull()?.isUser == false &&
                    currentMessages.firstOrNull()?.text?.startsWith("This app now downloads supported local models") == true
                ) {
                    currentMessages.drop(1)
                } else {
                    currentMessages
                }
            }
            .takeLast(CONTEXT_MESSAGE_LIMIT)

        if (recentMessages.isEmpty()) return ""

        val conversation = recentMessages.joinToString(separator = "\n") { message ->
            if (message.isUser) {
                "User: ${message.text}"
            } else {
                "Assistant: ${message.text}"
            }
        }

        return buildString {
            appendLine("Use the recent conversation context when it is relevant.")
            appendLine("Keep the answer concise and continue naturally from the chat.")
            appendLine()
            appendLine(conversation)
            append("Assistant:")
        }
    }

    private suspend fun ensureModelLoaded(): Boolean {
        if (loadedModelId == activeModel.id && isModelLoaded) return true

        val activeModelStatus = downloader.getDownloadStatus(activeModel)
        if (!activeModelStatus.isDownloaded) return false

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
        return if (model.id == downloadingModelId) {
            modelDownloadStatus
        } else {
            downloader.getDownloadStatus(model)
        }
    }

    fun isLoadingModel(model: ModelSpec): Boolean = loadingModelId == model.id && isModelLoading

    fun isLoadedModel(model: ModelSpec): Boolean = loadedModelId == model.id

    fun setSelectedAttachment(uri: Uri, displayName: String, mimeType: String?) {
        selectedAttachmentUri = uri
        selectedAttachmentName = displayName
        selectedAttachmentMimeType = mimeType
    }

    fun clearSelectedAttachment() {
        selectedAttachmentUri = null
        selectedAttachmentName = null
        selectedAttachmentMimeType = null
    }

    fun startNewChat() {
        currentSessionId = null
        messages.clear()
        setIntroMessageIfNeeded()
    }

    fun deleteChatSession(sessionId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                chatDao.deleteSession(sessionId)
            }

            if (currentSessionId == sessionId) {
                currentSessionId = null
                messages.clear()
                setIntroMessageIfNeeded()
            }

            loadChatSessions()
        }
    }

    fun loadChatSession(sessionId: Long) {
        currentSessionId = sessionId
        viewModelScope.launch {
            val session = withContext(Dispatchers.IO) {
                chatDao.getSessionById(sessionId)
            } ?: return@launch

            session.modelName.takeIf { it.isNotBlank() }?.let { modelName ->
                ModelCatalog.supportedModels.firstOrNull { it.displayName == modelName }?.let { model ->
                    activeModel = model
                    selectedModel = model.displayName
                    modelDownloadStatus = downloader.getDownloadStatus(model)
                }
            }

            val storedMessages = withContext(Dispatchers.IO) {
                chatDao.getMessagesForSession(session.id)
            }

            messages.clear()
            if (storedMessages.isEmpty()) {
                setIntroMessageIfNeeded()
            } else {
                messages.addAll(
                    storedMessages.map {
                        Message(
                            text = it.text,
                            isUser = it.isUser,
                            messageType = it.messageType,
                            imagePath = it.imagePath,
                            imageName = it.imageName
                        )
                    }
                )
            }
        }
    }

    private fun startDownloadPolling(model: ModelSpec) {
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

    private fun restoreLatestSession() {
        viewModelScope.launch {
            if (currentSessionId != null) {
                return@launch
            }

            val latestSession = withContext(Dispatchers.IO) {
                chatDao.getLatestSession()
            }

            if (latestSession == null) {
                return@launch
            }

            if (currentSessionId != null) {
                return@launch
            }

            currentSessionId = latestSession.id
            latestSession.modelName.takeIf { it.isNotBlank() }?.let { modelName ->
                ModelCatalog.supportedModels.firstOrNull { it.displayName == modelName }?.let { model ->
                    activeModel = model
                    selectedModel = model.displayName
                    modelDownloadStatus = downloader.getDownloadStatus(model)
                }
            }

            val storedMessages = withContext(Dispatchers.IO) {
                chatDao.getMessagesForSession(latestSession.id)
            }

            messages.clear()
            if (storedMessages.isEmpty()) {
                setIntroMessageIfNeeded()
            } else {
                messages.addAll(
                    storedMessages.map {
                        Message(
                            text = it.text,
                            isUser = it.isUser,
                            messageType = it.messageType,
                            imagePath = it.imagePath,
                            imageName = it.imageName
                        )
                    }
                )
            }
        }
    }

    private fun loadChatSessions() {
        viewModelScope.launch {
            chatSessions = withContext(Dispatchers.IO) {
                chatDao.getAllSessions()
            }
        }
    }

    private suspend fun persistMessage(
        text: String,
        isUser: Boolean,
        messageType: String = "text",
        imagePath: String? = null,
        imageName: String? = null
    ) {
        val sessionId = ensureSessionExists(firstMessageText = text)
        withContext(Dispatchers.IO) {
            chatDao.insertMessage(
                ChatMessageEntity(
                    sessionId = sessionId,
                    text = text,
                    isUser = isUser,
                    messageType = messageType,
                    imagePath = imagePath,
                    imageName = imageName,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        loadChatSessions()
    }

    private suspend fun ensureSessionExists(firstMessageText: String): Long {
        currentSessionId?.let { return it }

        val sessionTitle = firstMessageText
            .trim()
            .replace("\n", " ")
            .take(SESSION_TITLE_MAX_LENGTH)
            .ifBlank { "New chat" }

        val sessionId = withContext(Dispatchers.IO) {
            chatDao.insertSession(
                ChatSession(
                    title = sessionTitle,
                    createdAt = System.currentTimeMillis(),
                    modelName = activeModel.displayName
                )
            )
        }
        currentSessionId = sessionId
        loadChatSessions()
        return sessionId
    }

    fun isSelectedSession(sessionId: Long): Boolean = currentSessionId == sessionId

    private fun setIntroMessageIfNeeded() {
        if (messages.isEmpty()) {
            messages.add(
                Message(
                    "This app now downloads supported local models on demand. It can load MediaPipe .task models and LiteRT-LM .litertlm models directly on device.",
                    false
                )
            )
        }
    }

    private fun copyImageToAppStorage(uri: Uri, imageName: String): String {
        val safeName = imageName
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "image_${System.currentTimeMillis()}" }
        val imageDir = File(getApplication<Application>().filesDir, "chat_images").apply {
            mkdirs()
        }
        val targetFile = File(imageDir, "${System.currentTimeMillis()}_$safeName")
        getApplication<Application>().contentResolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to read selected image")
        return targetFile.absolutePath
    }

    override fun onCleared() {
        llm.close()
        super.onCleared()
    }
}
