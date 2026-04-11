package com.stafeewa.photocalorie.app.domain.entity

data class DailyIntake(
    val date: String,
    val meals: Map<MealType, List<FoodEntry>> = emptyMap(),
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalCarbs: Double = 0.0
)