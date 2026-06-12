package com.yashbhadange.tinyai.screens.chat

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.yashbhadange.tinyai.ai.LocalLLMManager
import com.yashbhadange.tinyai.ai.ModelCatalog
import com.yashbhadange.tinyai.ai.ModelDownloader
import com.yashbhadange.tinyai.data.api.HFRemoteModelGroup
import com.yashbhadange.tinyai.data.api.HuggingFaceModelsRepository
import com.yashbhadange.tinyai.data.database.AppDatabase
import com.yashbhadange.tinyai.data.database.ChatSession
import com.yashbhadange.tinyai.data.storage.MediaStorage

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    internal companion object {
        const val CONTEXT_MESSAGE_LIMIT = 4
        const val SESSION_TITLE_MAX_LENGTH = 40
    }

    internal val mediaStorage = MediaStorage(application)
    internal val llm = LocalLLMManager(application)
    internal val downloader = ModelDownloader(application)
    internal val remoteModelsRepository = HuggingFaceModelsRepository()
    internal val chatDao = AppDatabase.getDatabase(application).chatDao()

    internal var activeModel = ModelCatalog.defaultModel
    internal var downloadPollingJob: kotlinx.coroutines.Job? = null
    internal var loadedModelId: String? = null
    internal var loadedSessionId: Long? = null
    internal var downloadingModelId: String? = null
    internal var loadingModelId: String? = null

    var currentSessionId by mutableStateOf<Long?>(null)
        internal set

    val isNewChat: Boolean get() = currentSessionId == null

    internal val systemPromptDrafts = mutableStateMapOf<String, String>()

    val messages = mutableStateListOf<Message>()

    var selectedModel by mutableStateOf(activeModel.displayName)
        internal set

    var isModelLoading by mutableStateOf(false)
        internal set

    var isTyping by mutableStateOf(false)
        internal set

    var isModelLoaded by mutableStateOf(false)
        internal set

    var modelDownloadStatus by mutableStateOf(downloader.getDownloadStatus(activeModel))
        internal set

    var chatSessions by mutableStateOf<List<ChatSession>>(emptyList())
        internal set

    var remoteModelGroups by mutableStateOf<List<HFRemoteModelGroup>>(emptyList())
        internal set

    var selectedAttachmentUri by mutableStateOf<Uri?>(null)
        internal set

    var selectedAttachmentName by mutableStateOf<String?>(null)
        internal set

    var selectedAttachmentMimeType by mutableStateOf<String?>(null)
        internal set

    var currentSystemPrompt by mutableStateOf("")
        internal set

    var modelLoadIndicator by mutableStateOf<String?>(null)
        internal set

    val modelGpuPreferences = mutableStateMapOf<String, Boolean>()

    fun isGpuEnabledForModel(modelId: String): Boolean = modelGpuPreferences[modelId] ?: false

    init {
        this.setIntroMessageIfNeeded()
        this.refreshModelStatus()
        this.loadRemoteModelCatalog()
        this.loadChatSessions()
        this.restoreLatestSession()
    }

    override fun onCleared() {
        llm.close()
        super.onCleared()
    }
}
