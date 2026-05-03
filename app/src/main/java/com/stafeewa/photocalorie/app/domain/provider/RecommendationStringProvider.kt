package com.stafeewa.photocalorie.app.domain.provider

import com.stafeewa.photocalorie.app.domain.entity.MealType

interface RecommendationStringProvider {
    fun getLightSnackName(): String
    fun getLightSnackReason(): String
    fun getProteinDeficitReason(): String
    fun getBalanceReason(): String
    fun getLikedReason(): String
    fun getUniversalReason(): String
    fun getFallbackProductNames(): List<String>
    fun getCategoryName(category: String): String
    fun getMealTypeName(mealType: MealType): String
}