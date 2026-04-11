package com.stafeewa.photocalorie.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResultRecipesDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("image")
    val image: String = "",
    @SerialName("nutrition")
    val nutrition: NutritionDto = NutritionDto(),
    @SerialName("title")
    val title: String = "",
    @SerialName("sourceUrl")
    val sourceUrl: String = ""
)