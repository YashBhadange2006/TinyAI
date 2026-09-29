package com.yashbhadange.tinyai.ai.inference

import android.content.Context
import com.arm.aichat.AiChat
import com.arm.aichat.InferenceEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

class LlamaCppEngine(context : Context){
    private val engine = AiChat.getInferenceEngine(context)

    val state : StateFlow<InferenceEngine.State>
        get() = engine.state

    suspend fun loadModel(path: String) {
        state.first {
            it is InferenceEngine.State.Initialized || it is InferenceEngine.State.Error
        }
        engine.loadModel(path)
    }

    suspend fun setSystemPrompt(prompt: String) {
        engine.setSystemPrompt(prompt)
    }

    fun generate(
        prompt: String,
        maxTokens: Int = InferenceEngine.DEFAULT_PREDICT_LENGTH
    ) : Flow<String> {
        return engine.sendUserPrompt(prompt,maxTokens)
    }

    fun cleanUp() {
        engine.cleanUp()
    }

    fun destroy(){
        engine.destroy()
    }
}