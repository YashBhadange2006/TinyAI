package com.yashbhadange.tinyai.ai

import android.content.Context
import android.net.Uri
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.LogSeverity
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.genai.llminference.GraphOptions
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class ExecutionBackend {
    CPU,
    GPU
}

class LocalLLMManager(
    private val context: Context
) {
    private companion object {
        const val DEFAULT_MAX_TOKENS = 1024
    }

    private enum class ModelRuntime {
        MEDIAPIPE,
        LITERTLM
    }

    // variable editable only by this file
    var supportsVision: Boolean = false
        private set

    private var llmInference: LlmInference? = null
    private var liteRtEngine: Engine? = null
    private var liteRtConversation: Conversation? = null
    private var loadedModelPath: String? = null
    private var loadedRuntime: ModelRuntime? = null
    private var loadedSystemPrompt: String = ""
    var loadedBackend: ExecutionBackend? = null //first set to null later default will be CPU

    fun isLoaded(modelPath: String, systemPrompt: String = "", backend: ExecutionBackend): Boolean {
        val normalizedPrompt = systemPrompt.trim()
        return when (loadedRuntime) {
            ModelRuntime.MEDIAPIPE -> loadedModelPath == modelPath && llmInference != null && loadedBackend == backend
            ModelRuntime.LITERTLM -> {
                loadedModelPath == modelPath &&
                        loadedBackend == backend &&
                        loadedSystemPrompt == normalizedPrompt &&
                        liteRtEngine != null &&
                        liteRtConversation != null
            }
            null -> false
        }
    }

    fun loadModel(modelPath: String, systemPrompt: String = "", backend: ExecutionBackend = ExecutionBackend.CPU) {
        if (isLoaded(modelPath, systemPrompt,backend)) return

        close()

        if (modelPath.endsWith(".litertlm", ignoreCase = true)) {
            loadLiteRtLmModel(modelPath, systemPrompt,backend)
        } else {
            loadMediaPipeModel(modelPath,backend)
        }
        loadedModelPath = modelPath
        loadedSystemPrompt = systemPrompt.trim()
        loadedBackend = backend
    }

    fun generate(prompt: String): String {
        return try {
            when (loadedRuntime) {
                ModelRuntime.MEDIAPIPE -> {
                    val engine = llmInference
                        ?: return "MediaPipe model is not loaded yet."
                    engine.generateResponse(prompt)
                }

                ModelRuntime.LITERTLM -> {
                    val conversation = liteRtConversation
                        ?: return "LiteRT-LM model is not loaded yet."
                    conversation.sendMessage(prompt).toString()
                }

                null -> "Model is not loaded yet. Download and load a model first."
            }
        } catch (e: Exception) {
            "Failed to generate response: ${e.message ?: "unknown error"}"
        }
    }

    suspend fun generateStream(
        prompt: String,
        onPartial: (String) -> Unit
    ): String {
        return try {
            when (loadedRuntime) {
                ModelRuntime.MEDIAPIPE -> {
                    val engine = llmInference
                        ?: return "MediaPipe model is not loaded yet."
                    val future = engine.generateResponseAsync(
                        prompt,
                        ProgressListener<String> { partialResponse, _ ->
                            onPartial(partialResponse)
                        }
                    )
                    withContext(Dispatchers.IO) {
                        future.get()
                    }
                }

                ModelRuntime.LITERTLM -> {
                    val conversation = liteRtConversation
                        ?: return "LiteRT-LM model is not loaded yet."
                    var finalResponse = ""
                    conversation.sendMessageAsync(prompt).collect { partialMessage ->
                        val partialText = partialMessage.toString()
                        finalResponse = partialText
                        onPartial(partialText)
                    }
                    finalResponse
                }

                null -> "Model is not loaded yet. Download and load a model first."
            }
        } catch (e: Exception) {
            "Failed to generate response: ${e.message ?: "unknown error"}"
        }
    }

    fun generateWithImage(prompt: String, imageUri: Uri): String {
        if(!supportsVision){
            return "Error: The currently loaded model only supports text generation."
        }
        return try {
            when (loadedRuntime) {
                ModelRuntime.MEDIAPIPE -> {
                    val engine = llmInference
                        ?: return "MediaPipe model is not loaded yet."
                    val image = BitmapImageBuilder(context, imageUri).build()
                    val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
                        .setGraphOptions(
                            GraphOptions.builder()
                                .setEnableVisionModality(true)
                                .build()
                        )
                        .build()

                    LlmInferenceSession.createFromOptions(engine, sessionOptions).use { session ->
                        session.addImage(image)
                        session.addQueryChunk(prompt)
                        session.generateResponse()
                    }
                }

                ModelRuntime.LITERTLM -> {
                    val conversation = liteRtConversation
                        ?: return "LiteRT-LM model is not loaded yet."
                    val imagePath = cacheImageForLiteRt(imageUri)
                    val contents = Contents.Companion.of(
                        Content.ImageFile(imagePath),
                        Content.Text(prompt)
                    )
                    conversation.sendMessage(contents).toString()
                }

                null -> "Model is not loaded yet. Download and load a model first."
            }
        } catch (e: Exception) {
            "Failed to analyze image: ${e.message ?: "unknown error"}"
        }
    }

    fun close() {
        llmInference?.close()
        llmInference = null

        liteRtConversation?.close()
        liteRtConversation = null

        liteRtEngine?.close()
        liteRtEngine = null

        loadedRuntime = null
        loadedModelPath = null
        loadedSystemPrompt = ""
        supportsVision = false
    }

    private fun loadMediaPipeModel(modelPath: String, backend: ExecutionBackend) {

        val preferred = if(backend == ExecutionBackend.GPU) LlmInference.Backend.GPU else LlmInference.Backend.CPU
        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setPreferredBackend(preferred)
                .setMaxTokens(DEFAULT_MAX_TOKENS)
                .setMaxNumImages(1)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            supportsVision = true
        } catch (e: Exception){
            val textOptions = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(DEFAULT_MAX_TOKENS)
                .build()

            llmInference = LlmInference.createFromOptions(context, textOptions)
            supportsVision = false
        }
        loadedRuntime = ModelRuntime.MEDIAPIPE
    }

    private fun loadLiteRtLmModel(modelPath: String, systemPrompt: String, backend: ExecutionBackend) {
        val selectedBackend = if(backend== ExecutionBackend.GPU) Backend.GPU() else Backend.CPU()
        Engine.Companion.setNativeMinLogSeverity(LogSeverity.ERROR)

        var engine: Engine? = null
        try{
            val engineConfig = EngineConfig(
                modelPath = modelPath,
                backend = selectedBackend,
                visionBackend = selectedBackend,
                cacheDir = context.cacheDir.absolutePath
            )

            engine = Engine(engineConfig)
            engine.initialize()
            supportsVision = true
        } catch (e: Exception){
            val textConfig = EngineConfig(
                modelPath = modelPath,
                backend = Backend.CPU(),
                visionBackend = null,
                cacheDir = context.cacheDir.absolutePath
            )

            engine = Engine(textConfig)
            engine.initialize()
            supportsVision = false
        }


        liteRtEngine = engine
        val normalizedPrompt = systemPrompt.trim()
        liteRtConversation = if (normalizedPrompt.isNotEmpty()) {
            engine?.createConversation(
                ConversationConfig(
                    systemInstruction = Contents.of(normalizedPrompt)
                )
            )
        } else {
            engine?.createConversation()
        }
        loadedRuntime = ModelRuntime.LITERTLM
    }

    private fun cacheImageForLiteRt(imageUri: Uri): String {
        val targetFile = File(context.cacheDir, "litertlm_input_image")
        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            targetFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: error("Failed to read the selected image.")
        return targetFile.absolutePath
    }
}
