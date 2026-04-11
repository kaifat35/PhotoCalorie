package com.stafeewa.photocalorie.app.domain.usecase.recipe

import com.stafeewa.photocalorie.app.domain.repository.RecipeRepository
import javax.inject.Inject

class RemoveSubscriptionsUseCase @Inject constructor(
    private val recipesRepository: RecipeRepository
) {
    suspend operator fun invoke(topic: String) {
        recipesRepository.removeSubscription(topic)
    }
}