package com.stafeewa.photocalorie.app.data.remote


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NutritionDto(
    @SerialName("caloricBreakdown")
    val caloricBreakdown: CaloricBreakdownDto = CaloricBreakdownDto(),
    @SerialName("nutrients")
    val nutrients: List<NutrientXDto> = listOf(),
)