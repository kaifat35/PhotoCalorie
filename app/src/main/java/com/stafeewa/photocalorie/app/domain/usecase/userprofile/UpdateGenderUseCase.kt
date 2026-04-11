package com.stafeewa.photocalorie.app.domain.usecase.userprofile

import com.stafeewa.photocalorie.app.domain.repository.UserProfileRepository
import javax.inject.Inject


class UpdateGenderUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
){
    suspend operator fun invoke(gender: String? ) = userProfileRepository.updateGender(gender)
}