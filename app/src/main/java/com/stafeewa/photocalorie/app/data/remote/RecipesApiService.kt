package com.stafeewa.photocalorie.app.data.remote

import com.stafeewa.photocalorie.app.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface RecipesApiService {
    @GET("recipes/complexSearch")
    suspend fun loadRecipes(
        @Query("query") topic: String,
        @Query("language") language: String,
        @Query("apiKey") apiKey: String = BuildConfig.SPONACULAR_API_KEY,
        @Query("number") number: Int = 1,
        @Query("addRecipeNutrition") addNutrition: Boolean = true
    ): RecipesResponseDto
}

