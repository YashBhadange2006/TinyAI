package com.yashbhadange.tinyai.screens.chat

import androidx.lifecycle.viewModelScope
import com.yashbhadange.tinyai.ai.ModelCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun ChatViewModel.startNewChat() {
    unloadActiveConversation()
    currentSessionId = null
    activeModel = ModelCatalog.defaultModel
    selectedModel = activeModel.displayName
    currentSystemPrompt = ""
    systemPromptDrafts.clear()
    modelDownloadStatus = downloader.getDownloadStatus(activeModel)
    modelLoadIndicator = null
    messages.clear()
    setIntroMessageIfNeeded()
}

fun ChatViewModel.deleteChatSession(sessionId: Long) {
    viewModelScope.launch {
        withContext(Dispatchers.IO) {
            chatDao.deleteSession(sessionId)
        }

        if (currentSessionId == sessionId) {
            unloadActiveConversation()
            currentSessionId = null
            messages.clear()
            activeModel = ModelCatalog.defaultModel
            selectedModel = activeModel.displayName
            currentSystemPrompt = ""
            modelDownloadStatus = downloader.getDownloadStatus(activeModel)
            modelLoadIndicator = null
            setIntroMessageIfNeeded()
        }

        loadChatSessions()
    }
}

fun ChatViewModel.loadChatSession(sessionId: Long) {
    unloadActiveConversation()
    currentSessionId = sessionId
    viewModelScope.launch {
        val session = withContext(Dispatchers.IO) {
            chatDao.getSessionById(sessionId)
        } ?: return@launch

        activeModel = ModelCatalog.defaultModel
        selectedModel = activeModel.displayName
        modelDownloadStatus = downloader.getDownloadStatus(activeModel)
        currentSystemPrompt = session.systemPrompt
        session.modelName.takeIf { it.isNotBlank() }?.let { modelName ->
            findKnownModelByDisplayName(modelName)?.let { model ->
                activeModel = model
                selectedModel = model.displayName
                systemPromptDrafts[model.id] = session.systemPrompt
                modelDownloadStatus = downloader.getDownloadStatus(model)
                modelLoadIndicator = "Restoring"
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
                        imageName = it.imageName,
                        thinkingText = it.thinkingText
                    )
                }
            )
        }

        session.modelName.takeIf { it.isNotBlank() }?.let { modelName ->
            findKnownModelByDisplayName(modelName)?.let { model ->
                val loaded = loadModelForContext(
                    model = model,
                    systemPrompt = session.systemPrompt,
                    confirmationMessage = "Restored ${model.displayName} for this chat.",
                    persistSessionConfig = false
                )
                if (!loaded) {
                    messages.add(
                        Message(
                            "This chat uses ${model.displayName}, but it is not loaded yet. Download it from Model Settings to continue.",
                            false
                        )
                    )
                }
            }
        }
    }
}

fun ChatViewModel.loadChatSessions() {
    viewModelScope.launch {
        chatSessions = withContext(Dispatchers.IO) {
            chatDao.getAllSessions()
        }
    }
}

fun ChatViewModel.isSelectedSession(sessionId: Long): Boolean = currentSessionId == sessionId

internal fun ChatViewModel.setIntroMessageIfNeeded() {
    if (messages.isEmpty()) {
        messages.add(
            Message(
                "This app now downloads supported local models on demand. It can load MediaPipe .task models and LiteRT-LM .litertlm models directly on device.",
                false
            )
        )
    }
}

private fun ChatViewModel.restoreLatestSessionInternal() {
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
        activeModel = ModelCatalog.defaultModel
        selectedModel = activeModel.displayName
        modelDownloadStatus = downloader.getDownloadStatus(activeModel)
        currentSystemPrompt = latestSession.systemPrompt
        latestSession.modelName.takeIf { it.isNotBlank() }?.let { modelName ->
            findKnownModelByDisplayName(modelName)?.let { model ->
                activeModel = model
                selectedModel = model.displayName
                systemPromptDrafts[model.id] = latestSession.systemPrompt
                modelDownloadStatus = downloader.getDownloadStatus(model)
                modelLoadIndicator = "Restoring"
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
                        imageName = it.imageName,
                        thinkingText = it.thinkingText
                    )
                }
            )
        }

        latestSession.modelName.takeIf { it.isNotBlank() }?.let { modelName ->
            findKnownModelByDisplayName(modelName)?.let { model ->
                val loaded = loadModelForContext(
                    model = model,
                    systemPrompt = latestSession.systemPrompt,
                    confirmationMessage = "Restored ${model.displayName} for this chat.",
                    persistSessionConfig = false
                )
                if (!loaded) {
                    messages.add(
                        Message(
                            "This chat uses ${model.displayName}, but it is not loaded yet. Download it from Model Settings to continue.",
                            false
                        )
                    )
                }
            }
        }
    }
}

fun ChatViewModel.restoreLatestSession() {
    restoreLatestSessionInternal()
}
