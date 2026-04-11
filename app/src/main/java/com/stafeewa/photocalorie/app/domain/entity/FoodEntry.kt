package com.stafeewa.photocalorie.app.domain.entity

data class FoodEntry(
    val id: Long = 0,
    val name: String,
    val mealType: MealType,
    val portion: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    val calories: Double
        get() = (protein * 4) + (fat * 9) + (carbs * 4)

    val formattedCalories: String
        get() = "${calories.toInt()} ккал"
}