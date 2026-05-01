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
        id = "gemma4_e2b",
        displayName = "Gemma 4 E2B",
        sizeLabel = "Hosted download",
        downloadUrl = "https://huggingface.co/Bioniok/LocalModel/resolve/main/gemma-4-E2B-it-web.task",
        fileName = "gemma-4-E2B-it-web.task",
        description = "Downloads the hosted MediaPipe .task bundle on demand, then loads it locally on device."
    )

    val supportedModels = listOf(tinyLlama)
    val defaultModel = tinyLlama
}
