package com.yashbhadange.tinyai.data.api

import androidx.annotation.Keep
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Keep
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
