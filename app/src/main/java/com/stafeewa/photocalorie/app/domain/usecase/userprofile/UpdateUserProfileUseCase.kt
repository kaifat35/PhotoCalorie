package com.stafeewa.photocalorie.app.domain.usecase.userprofile

import com.stafeewa.photocalorie.app.domain.repository.UserProfileRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    suspend operator fun invoke(
        login: String? = null,
        email: String? = null,
        password: String? = null,
        gender: String? = null,
        height: Double? = null,
        weight: Double? = null,
        age: Int? = null,
        imageUri: String? = null,
        dailyCalories: Double? = null
    ) = userProfileRepository.updateProfile(
        login, email, password, gender, height, weight, age, imageUri, dailyCalories
    )
}