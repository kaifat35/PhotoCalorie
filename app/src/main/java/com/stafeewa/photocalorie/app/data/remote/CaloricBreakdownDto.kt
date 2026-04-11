package com.stafeewa.photocalorie.app.data.remote


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CaloricBreakdownDto(
    @SerialName("percentCarbs")
    val percentCarbs: Double = 0.0,
    @SerialName("percentFat")
    val percentFat: Double = 0.0,
    @SerialName("percentProtein")
    val percentProtein: Double = 0.0
)