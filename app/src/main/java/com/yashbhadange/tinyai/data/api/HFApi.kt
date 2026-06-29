package com.yashbhadange.tinyai.data.api

import androidx.annotation.Keep
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Keep
interface HFApi {
    @GET("api/models")
    suspend fun fetchLiteRTModels(
        @Query("author") author: String?,
        @Query("expand") expand: String?,
        @Query("limit") limit: Int?,
        @Query("sort") sort: String?,
        @Query("direction") direction: String?
    ): List<HFModel>

    @GET("api/models/{repoId}")
    suspend fun fetchModelInfo(
        @Path(value = "repoId", encoded = true) repoId: String,
        @Query("expand") expand: String?
    ): HFModel
}