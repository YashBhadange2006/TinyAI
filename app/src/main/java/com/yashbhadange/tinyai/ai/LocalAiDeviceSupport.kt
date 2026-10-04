package com.yashbhadange.tinyai.ai

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import java.io.File

object LocalAiDeviceSupport {
    private const val MODEL_MEMORY_MULTIPLIER = 1.25
    private const val MAX_32_BIT_GGUF_BYTES = 512L * 1024 * 1024

    fun unsupportedReason(context: Context, modelPath: String): String? {
        val is32BitOnly = Build.SUPPORTED_64_BIT_ABIS.isEmpty()
        val modelFile = File(modelPath)
        if (is32BitOnly && (!modelPath.endsWith(".gguf", ignoreCase = true) ||
                modelFile.length() > MAX_32_BIT_GGUF_BYTES)) {
            return "This device supports GGUF models up to 512 MB."
        }

        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(ActivityManager::class.java)
        activityManager.getMemoryInfo(memoryInfo)
        val requiredBytes = (modelFile.length() * MODEL_MEMORY_MULTIPLIER).toLong()
        if (requiredBytes > 0 && memoryInfo.availMem < requiredBytes) {
            return "Not enough free memory to load this model."
        }

        return null
    }
}
