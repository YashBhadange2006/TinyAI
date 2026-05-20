package com.example.localmodelai.chatui

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.LogSeverity
import com.google.ai.edge.litertlm.Conversation
import com.google.mediapipe.tasks.genai.llminference.LlmInference

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
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
        loadedRuntime = ModelRuntime.MEDIAPIPE
    }

    private fun loadLiteRtLmModel(modelPath: String) {
        Engine.setNativeMinLogSeverity(LogSeverity.ERROR)

        val engineConfig = EngineConfig(
            modelPath = modelPath,
            backend = Backend.CPU(),
            cacheDir = context.cacheDir.absolutePath
        )

        val engine = Engine(engineConfig)
        engine.initialize()

        liteRtEngine = engine
        liteRtConversation = engine.createConversation()
        loadedRuntime = ModelRuntime.LITERTLM
    }
}
