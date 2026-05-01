package com.example.localmodelai.chatui

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference

class LocalLLMManager(
    private val context: Context
) {
    private var llmInference: LlmInference? = null
    private var loadedModelPath: String? = null

    fun isLoaded(modelPath: String): Boolean = loadedModelPath == modelPath && llmInference != null

    fun loadModel(modelPath: String) {
        if (isLoaded(modelPath)) return

        llmInference?.close()

        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(256)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
        loadedModelPath = modelPath
    }

    fun generate(prompt: String): String {
        val engine = llmInference
            ?: return "Model is not loaded yet. Download and load TinyLlama first."

        return try {
            engine.generateResponse(prompt)
        } catch (e: Exception) {
            "Failed to generate response: ${e.message ?: "unknown error"}"
        }
    }
}
