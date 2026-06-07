package com.example.localmodelai.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HFApi{
    @GET("api/models")
    suspend fun fetchLiteRTModels(
        @Query("author")author: String = "litert-community",
        @Query("expand") expand: String = "siblings",
        @Query("limit") limit: Int = 30,
        @Query("sort") sort: String = "downloads",
        @Query("direction") direction: String = "-1"
    ): List<HFModel>

    @GET("api/models/{repoId}")
    suspend fun fetchModelInfo(
        @Path(value = "repoId", encoded = true) repoId: String,
        @Query("expand") expand: String = "siblings"
    ): HFModel
}
