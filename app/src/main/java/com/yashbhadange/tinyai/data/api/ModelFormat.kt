package com.yashbhadange.tinyai.data.api

enum class ModelFormat(
    val extension: String,
    val hfFilter: String?
) {
    LITERTLM(
        extension = ".litertlm",
        hfFilter = "litert-lm"
    ),

    TASK(
        extension = ".task",
        hfFilter = "mediapipe"
    ),

    GGUF(
        extension = ".gguf",
        hfFilter = "gguf"
    )
}
