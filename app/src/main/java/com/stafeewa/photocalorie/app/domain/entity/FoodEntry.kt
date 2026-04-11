package com.stafeewa.photocalorie.app.domain.entity

data class FoodEntry(
    val id: Int,
    val name: String,
    val mealType: MealType,
    val portion: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    val calories: Double
        get() = protein * 4 + fat * 9 + carbs * 4
}

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK
}