package com.yashbhadange.tinyai.ai

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import java.io.File

object LocalAiDeviceSupport {
    private const val MODEL_MEMORY_MULTIPLIER = 1.25

    fun unsupportedReason(context: Context, modelPath: String): String? {
        if (Build.SUPPORTED_64_BIT_ABIS.isEmpty()) {
            return "Local AI models require a 64-bit device."
        }

        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(ActivityManager::class.java)
        activityManager.getMemoryInfo(memoryInfo)
        val requiredBytes = (File(modelPath).length() * MODEL_MEMORY_MULTIPLIER).toLong()
        if (requiredBytes > 0 && memoryInfo.availMem < requiredBytes) {
            return "Not enough free memory to load this model."
        }

        return null
    }
}
