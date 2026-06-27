package com.yashbhadange.tinyai.screens.chat

import androidx.lifecycle.viewModelScope
import com.yashbhadange.tinyai.data.database.ChatMessageEntity
import com.yashbhadange.tinyai.data.database.ChatSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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
            val assistantMessageIndex = messages.size
            messages.add(Message(text = "...", isUser = false, isStreaming = true))
            var streamedReply = ""
            val reply = withContext(Dispatchers.IO) {
                when {
                    attachmentUri == null -> "No image was selected."
                    attachmentMimeType?.startsWith("image/") != true -> {
                        "Only image attachments are supported right now."
                    }
                    else -> {
                        llm.generateWithImageStream(effectivePrompt, attachmentUri) { partialReply ->
                            if (partialReply.isEmpty()) {
                                return@generateWithImageStream
                            }
                            streamedReply = mergeStreamChunk(
                                currentText = streamedReply,
                                incomingChunk = partialReply
                            )
                            val parsedReply = splitThinkingFromAnswer(streamedReply)
                            viewModelScope.launch {
                                updateAssistantMessage(
                                    index = assistantMessageIndex,
                                    text = parsedReply.answer.ifBlank {
                                        if (parsedReply.thinking.isNotBlank()) "..." else streamedReply
                                    },
                                    isStreaming = true,
                                    thinkingText = parsedReply.thinking
                                )
                            }
                        }
                    }
                }
            }
            val finalText = when {
                reply.isNotBlank() -> mergeStreamChunk(
                    currentText = streamedReply,
                    incomingChunk = reply
                )
                streamedReply.isNotBlank() -> streamedReply
                else -> ""
            }
            val parsedReply = splitThinkingFromAnswer(finalText)
            updateAssistantMessage(
                index = assistantMessageIndex,
                text = parsedReply.answer.ifBlank { if (parsedReply.thinking.isNotBlank()) "..." else "No response generated." },
                isStreaming = false,
                thinkingText = parsedReply.thinking
            )
            persistMessage(
                text = parsedReply.answer.ifBlank { "No response generated." },
                isUser = false,
                thinkingText = parsedReply.thinking
            )
            clearSelectedAttachment()
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
                val parsedReply = splitThinkingFromAnswer(streamedReply)
                viewModelScope.launch {
                    updateAssistantMessage(
                        index = assistantMessageIndex,
                        text = parsedReply.answer.ifBlank {
                            if (parsedReply.thinking.isNotBlank()) "..." else streamedReply
                        },
                        isStreaming = true,
                        thinkingText = parsedReply.thinking
                    )
                }
            }
        }
        val finalText = when {
            reply.isNotBlank() -> mergeStreamChunk(
                currentText = streamedReply,
                incomingChunk = reply
            )
            streamedReply.isNotBlank() -> streamedReply
            else -> ""
        }
        val parsedFinalReply = splitThinkingFromAnswer(finalText)
        updateAssistantMessage(
            index = assistantMessageIndex,
            text = parsedFinalReply.answer.ifBlank { if (parsedFinalReply.thinking.isNotBlank()) "..." else "No response generated." },
            isStreaming = false,
            thinkingText = parsedFinalReply.thinking
        )
        persistMessage(
            text = parsedFinalReply.answer.ifBlank { "No response generated." },
            isUser = false,
            thinkingText = parsedFinalReply.thinking
        )
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
    imageName: String? = null,
    thinkingText: String = ""
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
                thinkingText = thinkingText,
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
    isStreaming: Boolean,
    thinkingText: String = ""
) {
    if (index in messages.indices) {
        messages[index] = Message(
            text = text,
            isUser = false,
            isStreaming = isStreaming,
            thinkingText = thinkingText
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

private data class ThinkingParseResult(
    val answer: String,
    val thinking: String
)

private fun splitThinkingFromAnswer(rawText: String): ThinkingParseResult {
    val normalized = rawText
        .replace("\r\n", "\n")
        .trim()

    val thinkStart = normalized.indexOf("<think>", ignoreCase = true)
    if (thinkStart == -1) {
        return ThinkingParseResult(
            answer = normalized
                .replace("<endofuser>", "", ignoreCase = true)
                .replace("</think>", "", ignoreCase = true)
                .trim(),
            thinking = ""
        )
    }

    val afterStart = thinkStart + "<think>".length
    val thinkEndTag = normalized.indexOf("</think>", startIndex = afterStart, ignoreCase = true)
    val endOfUserTag = normalized.indexOf("<endofuser>", startIndex = afterStart, ignoreCase = true)
    val markerIndex = when {
        thinkEndTag >= 0 && endOfUserTag >= 0 -> minOf(thinkEndTag, endOfUserTag)
        thinkEndTag >= 0 -> thinkEndTag
        endOfUserTag >= 0 -> endOfUserTag
        else -> -1
    }

    return if (markerIndex >= 0) {
        val thinking = normalized.substring(afterStart, markerIndex).trim()
        val markerLength = when {
            markerIndex == thinkEndTag -> "</think>".length
            else -> "<endofuser>".length
        }
        val answer = normalized
            .substring(markerIndex + markerLength)
            .replace("<endofuser>", "", ignoreCase = true)
            .replace("</think>", "", ignoreCase = true)
            .trim()
        ThinkingParseResult(
            answer = answer,
            thinking = thinking
        )
    } else {
        ThinkingParseResult(
            answer = "",
            thinking = normalized.substring(afterStart).trim()
        )
    }
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
