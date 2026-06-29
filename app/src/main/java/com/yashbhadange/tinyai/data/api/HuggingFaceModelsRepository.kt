package com.yashbhadange.tinyai.data.api

class HuggingFaceModelsRepository(
    private val api: HFApi = RetrofitClient.api
) {
    suspend fun fetchRemoteLiteRtModels(): List<HFRemoteModelGroup> {
        return api.fetchLiteRTModels(
            author = "litert-community",
            expand = "siblings",
            limit = 100,
            sort = "downloads",
            direction = "-1"
        ).mapNotNull { it.toRemoteGroup() }
    }
}