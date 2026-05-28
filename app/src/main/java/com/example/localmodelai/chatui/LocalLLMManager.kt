package com.example.localmodelai.chatui

import android.content.Context
import android.net.Uri
import java.io.File
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.LogSeverity
import com.google.ai.edge.litertlm.Conversation
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.google.mediapipe.tasks.genai.llminference.GraphOptions
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

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

    private var llmInference: LlmInference? = null
    private var liteRtEngine: Engine? = null
    private var liteRtConversation: Conversation? = null
    private var loadedModelPath: String? = null
    private var loadedRuntime: ModelRuntime? = null

    fun isLoaded(modelPath: String): Boolean {
        return when (loadedRuntime) {
            ModelRuntime.MEDIAPIPE -> loadedModelPath == modelPath && llmInference != null
            ModelRuntime.LITERTLM -> loadedModelPath == modelPath && liteRtEngine != null && liteRtConversation != null
            null -> false
        }
    }

    fun loadModel(modelPath: String) {
        if (isLoaded(modelPath)) return

        close()

        if (modelPath.endsWith(".litertlm", ignoreCase = true)) {
            loadLiteRtLmModel(modelPath)
        } else {
            loadMediaPipeModel(modelPath)
        }
        loadedModelPath = modelPath
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
                    val contents = Contents.of(
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
    }

    private fun loadMediaPipeModel(modelPath: String) {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(DEFAULT_MAX_TOKENS)
            .setMaxNumImages(1)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
        loadedRuntime = ModelRuntime.MEDIAPIPE
    }

    private fun loadLiteRtLmModel(modelPath: String) {
        Engine.setNativeMinLogSeverity(LogSeverity.ERROR)

        val engineConfig = EngineConfig(
            modelPath = modelPath,
            backend = Backend.CPU(),
            visionBackend = Backend.CPU(),
            cacheDir = context.cacheDir.absolutePath
        )

        val engine = Engine(engineConfig)
        engine.initialize()

        liteRtEngine = engine
        liteRtConversation = engine.createConversation()
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
