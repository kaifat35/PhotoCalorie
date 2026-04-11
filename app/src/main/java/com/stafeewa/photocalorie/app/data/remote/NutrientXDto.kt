package com.stafeewa.photocalorie.app.data.remote


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NutrientXDto(
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("name")
    val name: String = "",
    @SerialName("percentOfDailyNeeds")
    val percentOfDailyNeeds: Double = 0.0,
    @SerialName("unit")
    val unit: String = ""
)