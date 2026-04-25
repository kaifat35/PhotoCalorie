package com.stafeewa.photocalorie.app.domain.usecase.userprofile

import javax.inject.Inject

class CalculateTDEEUseCase @Inject constructor() {
    operator fun invoke(
        gender: String,
        heightCm: Double,
        weightKg: Double,
        age: Int,
        activityFactor: Double = 1.2   // по умолчанию сидячий образ жизни
    ): Double {
        val bmr = if (gender.equals("Мужской", ignoreCase = true)) {
            10 * weightKg + 6.25 * heightCm - 5 * age + 5
        } else {
            10 * weightKg + 6.25 * heightCm - 5 * age - 161
        }
        return bmr * activityFactor
    }
}