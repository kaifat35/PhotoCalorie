package com.stafeewa.photocalorie.app.domain.entity

data class UserProfile(
    val login: String,
    val email: String,
    val password: String,
    val gender: String? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val age: Int? = null,
    val imageUri: String? = null,
    val dailyCalories: Double? = null,
    val userId: Int?
)
