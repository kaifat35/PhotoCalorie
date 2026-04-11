package com.stafeewa.photocalorie.app.domain.usecase.recipe

import com.stafeewa.photocalorie.app.domain.repository.RecipeRepository
import javax.inject.Inject

class GetAllSubscriptionsUseCase @Inject constructor(
    private val recipesRepository: RecipeRepository
) {
    operator fun invoke() = recipesRepository.getAllSubscription()
}