package com.stafeewa.photocalorie.app.domain.entity

data class NutritionInfo(
    val calories: String = "",
    val protein: String = "",
    val fat: String = "",
    val carbs: String = ""
) {
    companion object {
        val Empty = NutritionInfo()
    }
}
