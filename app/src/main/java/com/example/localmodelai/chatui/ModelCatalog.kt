package com.example.localmodelai.chatui

data class ModelSpec(
    val id: String,
    val displayName: String,
    val sizeLabel: String,
    val downloadUrl: String,
    val fileName: String,
    val description: String
)

object ModelCatalog {
    val tinyLlama = ModelSpec(
        id = "tinyllama_task",
        displayName = "TinyLlama 1.1B Chat",
        sizeLabel = "Hosted download",
        downloadUrl = "https://huggingface.co/Bioniok/LocalModel/resolve/main/TinyLlama-1.1B-Chat-v1.0_multi-prefill-seq_q8_ekv1280.task?download=true",
        fileName = "TinyLlama-1.1B-Chat-v1.0_multi-prefill-seq_q8_ekv1280.task",
        description = "Downloads the hosted MediaPipe .task model on demand, then loads it locally on device."
    )

    val supportedModels = listOf(tinyLlama)
    val defaultModel = tinyLlama
}
