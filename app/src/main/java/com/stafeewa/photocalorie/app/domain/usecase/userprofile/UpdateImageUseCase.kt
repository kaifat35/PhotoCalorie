package com.stafeewa.photocalorie.app.domain.usecase.userprofile

import com.stafeewa.photocalorie.app.domain.repository.UserProfileRepository
import javax.inject.Inject


class UpdateImageUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
){
    suspend operator fun invoke(imageUri: String?) = userProfileRepository.updateImage(imageUri)
}