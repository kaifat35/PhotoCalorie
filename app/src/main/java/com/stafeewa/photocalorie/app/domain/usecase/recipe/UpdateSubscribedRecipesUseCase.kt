package com.stafeewa.photocalorie.app.domain.usecase.recipe

import com.stafeewa.photocalorie.app.domain.repository.RecipeRepository
import com.stafeewa.photocalorie.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateSubscribedRecipesUseCase @Inject constructor(
    private val recipesRepository: RecipeRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): List<String> {
        val settings = settingsRepository.getSettings().first()
        return recipesRepository.updateRecipesForAllSubscriptions(settings.language)
    }
}