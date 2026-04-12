package com.stafeewa.photocalorie.app.domain.entity

data class Product(
    val id: Long = 0,
    val name: String,
    val mealType: MealType,  // Для какого приёма пищи подходит
    val defaultPortion: Double = 100.0,  // Стандартная порция в граммах
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
    val caloriesPer100g: Double
) {
    fun calculateKbjuForPortion(portionInGrams: Double): KbjuValues {
        val factor = portionInGrams / 100.0
        return KbjuValues(
            calories = caloriesPer100g * factor,
            protein = proteinPer100g * factor,
            fat = fatPer100g * factor,
            carbs = carbsPer100g * factor
        )
    }
}

data class KbjuValues(
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)