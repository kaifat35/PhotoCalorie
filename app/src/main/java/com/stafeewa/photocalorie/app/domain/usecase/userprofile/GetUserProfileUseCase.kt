package com.stafeewa.photocalorie.app.domain.usecase.userprofile

import com.stafeewa.photocalorie.app.domain.repository.UserProfileRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
){
    operator fun invoke() = userProfileRepository.getUserProfile()
}