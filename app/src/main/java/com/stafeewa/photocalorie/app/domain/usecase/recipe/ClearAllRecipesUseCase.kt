package com.stafeewa.photocalorie.app.domain.usecase.recipe

import com.stafeewa.photocalorie.app.domain.repository.RecipeRepository
import javax.inject.Inject

class ClearAllRecipesUseCase @Inject constructor(
    private val recipesRepository: RecipeRepository
) {
    suspend operator fun invoke(topics: List<String>) {
        recipesRepository.clearAllRecipes(topics)
    }
}