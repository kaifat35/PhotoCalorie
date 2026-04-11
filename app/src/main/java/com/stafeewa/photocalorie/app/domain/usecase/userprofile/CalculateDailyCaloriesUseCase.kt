package com.stafeewa.photocalorie.app.domain.usecase.userprofile

import com.stafeewa.photocalorie.app.domain.entity.ActivityLevel
import com.stafeewa.photocalorie.app.domain.repository.UserProfileRepository
import javax.inject.Inject

class CalculateDailyCaloriesUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    suspend operator fun invoke(
        gender: String,
        height: Double,
        weight: Double,
        age: Int,
        activityLevel: ActivityLevel = ActivityLevel.ACTIVE
    ): Double {
        return userProfileRepository.calculateDailyCalories(
            gender = gender,
            height = height,
            weight = weight,
            age = age,
            activityLevel = activityLevel.multiplier
        )
    }
}