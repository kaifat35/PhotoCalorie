package com.stafeewa.photocalorie.app.domain.usecase.userprofile

import com.stafeewa.photocalorie.app.domain.entity.ActivityLevel
import com.stafeewa.photocalorie.app.domain.repository.UserProfileRepository
import javax.inject.Inject

class UpdateActivityLevelUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(level: ActivityLevel) {
        repository.updateActivityLevel(level)
    }
}