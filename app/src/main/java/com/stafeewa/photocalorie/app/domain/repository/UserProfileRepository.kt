package com.stafeewa.photocalorie.app.domain.repository

import com.stafeewa.photocalorie.app.domain.entity.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {

    fun getUserProfile(): Flow<UserProfile>

    suspend fun updateAge(age: Int)

    suspend fun updateDailyCalories(dailyCalories: Double?)

    suspend fun updateGender(gender: String?)

    suspend fun updateHeight(height: Double?)

    suspend fun updateImage(imageUri: String?)

    suspend fun updateLogin(login: String)

    suspend fun updateMail(email: String)

    suspend fun updatePassword(password: String)

    suspend fun updateWeight(weight: Double?)

    suspend fun updateProfile(
        login: String? = null,
        email: String? = null,
        password: String? = null,
        gender: String? = null,
        height: Double? = null,
        weight: Double? = null,
        age: Int? = null,
        imageUri: String? = null,
        dailyCalories: Double? = null
    )

    suspend fun deleteProfile(userId: Int?)
    suspend fun calculateDailyCalories(
        gender: String,
        height: Double,
        weight: Double,
        age: Int,
        activityLevel: Double = 1.2
    ): Double
}