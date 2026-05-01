package com.stafeewa.photocalorie.app.domain.entity

data class NutritionStatistics(
    val avgCalories: Double,
    val avgProtein: Double,
    val avgFat: Double,
    val avgCarbs: Double,
    val totalDays: Int
)