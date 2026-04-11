package com.stafeewa.photocalorie.app.domain.entity

data class Recipe (
    val id: Int,
    val image: String,
    val nutrition: NutritionInfo,
    val title: String,
    val sourceUrl: String
)
