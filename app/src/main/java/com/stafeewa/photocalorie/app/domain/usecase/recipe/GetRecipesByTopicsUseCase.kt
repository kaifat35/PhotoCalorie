package com.stafeewa.photocalorie.app.domain.usecase.recipe

import com.stafeewa.photocalorie.app.domain.entity.Recipe
import com.stafeewa.photocalorie.app.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecipesByTopicsUseCase @Inject constructor(
    private val recipesRepository: RecipeRepository
) {
    operator fun invoke(topics: List<String>): Flow<List<Recipe>> {
        return recipesRepository.getRecipesByTopics(topics)
    }
}