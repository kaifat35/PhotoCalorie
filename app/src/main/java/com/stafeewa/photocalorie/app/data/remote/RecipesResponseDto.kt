package com.stafeewa.photocalorie.app.data.remote


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipesResponseDto(
    @SerialName("results")
    val results: List<ResultRecipesDto> = listOf()
)