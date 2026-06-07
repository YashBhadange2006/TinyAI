package com.example.localmodelai.data.api

class HuggingFaceModelsRepository(
    private val api: HFApi = RetrofitClient.api
) {
    suspend fun fetchRemoteLiteRtModels(): List<HFRemoteModelGroup> {
        return api.fetchLiteRTModels(limit = 100)
            .mapNotNull { it.toRemoteGroup() }
    }
}
