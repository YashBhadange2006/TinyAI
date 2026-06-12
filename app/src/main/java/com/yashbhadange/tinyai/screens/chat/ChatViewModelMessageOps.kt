package com.yashbhadange.tinyai.screens.chat

import androidx.lifecycle.viewModelScope
import com.yashbhadange.tinyai.data.database.ChatMessageEntity
import com.yashbhadange.tinyai.data.database.ChatSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun ChatViewModel.sendMessage(prompt: String) {
    if (selectedAttachmentUri != null) {
        val attachmentUri = selectedAttachmentUri
        val attachmentName = selectedAttachmentName ?: "Selected image"
        val attachmentMimeType = selectedAttachmentMimeType
        val effectivePrompt = prompt.ifBlank { "Describe this image." }
        val userMessage = effectivePrompt

        viewModelScope.launch {
            val savedImagePath = withContext(Dispatchers.IO) {
                attachmentUri?.let { mediaStorage.saveChatImage(it, attachmentName) }
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
                    Message("The model is not ready yet. Download and load AI model from the model menu in the Settings icon first.", false)
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
                Message("The model is not ready yet. Download and load AI model from the model menu in the Settings icon first.", false)
            )
            return@launch
        }

        persistMessage(prompt, isUser = true)
        val contextualPrompt = buildContextualPrompt()
        val assistantMessageIndex = messages.size
        messages.add(Message(text = "...", isUser = false, isStreaming = true))
        var streamedReply = ""
        val reply = withContext(Dispatchers.IO) {
            llm.generateStream(contextualPrompt) { partialReply ->
                if (partialReply.isEmpty()) {
                    return@generateStream
                }
                streamedReply = mergeStreamChunk(
                    currentText = streamedReply,
                    incomingChunk = partialReply
                )
                viewModelScope.launch {
                    updateAssistantMessage(
                        index = assistantMessageIndex,
                        text = streamedReply,
                        isStreaming = true
                    )
                }
            }
        }
        val finalReply = when {
            reply.isNotBlank() -> mergeStreamChunk(
                currentText = streamedReply,
                incomingChunk = reply
            )
            streamedReply.isNotBlank() -> streamedReply
            else -> "No response generated."
        }
        updateAssistantMessage(
            index = assistantMessageIndex,
            text = finalReply,
            isStreaming = false
        )
        persistMessage(finalReply, isUser = false)
    }
}

private fun ChatViewModel.buildContextualPrompt(): String {
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
        .takeLast(ChatViewModel.CONTEXT_MESSAGE_LIMIT)

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

private suspend fun ChatViewModel.persistMessage(
    text: String,
    isUser: Boolean,
    messageType: String = "text",
    imagePath: String? = null,
    imageName: String? = null
) {
    val sessionId = ensureSessionExists(firstMessageText = text)
    if (loadedSessionId == null && isModelLoaded) {
        loadedSessionId = sessionId
    }
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

private suspend fun ChatViewModel.ensureSessionExists(firstMessageText: String): Long {
    currentSessionId?.let { return it }

    val sessionTitle = firstMessageText
        .trim()
        .replace("\n", " ")
        .take(ChatViewModel.SESSION_TITLE_MAX_LENGTH)
        .ifBlank { "New chat" }

    val sessionId = withContext(Dispatchers.IO) {
        chatDao.insertSession(
            ChatSession(
                title = sessionTitle,
                createdAt = System.currentTimeMillis(),
                modelName = activeModel.displayName,
                systemPrompt = currentSystemPrompt
            )
        )
    }
    currentSessionId = sessionId
    loadChatSessions()
    return sessionId
}

private fun ChatViewModel.ensureModelLoaded(): Boolean {
    val isSameUnsavedChat = loadedSessionId == null && currentSessionId == null
    val isSameSavedChat = loadedSessionId != null && loadedSessionId == currentSessionId
    return loadedModelId == activeModel.id && isModelLoaded && (isSameUnsavedChat || isSameSavedChat)
}

private fun ChatViewModel.updateAssistantMessage(
    index: Int,
    text: String,
    isStreaming: Boolean
) {
    if (index in messages.indices) {
        messages[index] = Message(
            text = text,
            isUser = false,
            isStreaming = isStreaming
        )
    }
}

private fun ChatViewModel.mergeStreamChunk(currentText: String, incomingChunk: String): String {
    if (incomingChunk.isEmpty()) {
        return currentText
    }
    if (incomingChunk.startsWith(currentText)) {
        return incomingChunk
    }
    return currentText + incomingChunk
}

fun ChatViewModel.setSelectedAttachment(uri: android.net.Uri, displayName: String, mimeType: String?) {
    selectedAttachmentUri = uri
    selectedAttachmentName = displayName
    selectedAttachmentMimeType = mimeType
}

fun ChatViewModel.clearSelectedAttachment() {
    selectedAttachmentUri = null
    selectedAttachmentName = null
    selectedAttachmentMimeType = null
}
