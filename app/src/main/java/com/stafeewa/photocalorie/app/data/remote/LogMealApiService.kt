package com.stafeewa.photocalorie.app.data.remote

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface LogMealApiService {

    @Multipart
    @POST("v2/image/segmentation/complete")
    suspend fun completeSegmentation(
        @Header("Authorization") authorization: String,
        @Part image: MultipartBody.Part
    ): JsonObject

    @POST("v2/nutrition/recipe/ingredients")
    suspend fun recipeIngredients(
        @Header("Authorization") authorization: String,
        @Body request: LogMealImageRequest
    ): JsonObject

    @POST("v2/nutrition/recipe/nutritionalInfo")
    suspend fun recipeNutritionalInfo(
        @Header("Authorization") authorization: String,
        @Body request: LogMealImageRequest
    ): JsonObject

    @GET("v2/history/getIntakesList")
    suspend fun getIntakesList(
        @Header("Authorization") authorization: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): JsonObject
}

@Serializable
data class LogMealImageRequest(
    val imageId: String
)
