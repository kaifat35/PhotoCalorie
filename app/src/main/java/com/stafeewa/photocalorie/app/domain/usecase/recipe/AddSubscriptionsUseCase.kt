package com.stafeewa.photocalorie.app.domain.usecase.recipe

import com.stafeewa.photocalorie.app.domain.repository.RecipeRepository
import com.stafeewa.photocalorie.app.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddSubscriptionsUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(topic: String) {
        recipeRepository.addSubscription(topic)
        CoroutineScope(currentCoroutineContext()).launch {
            val settings = settingsRepository.getSettings().first()
            recipeRepository.updateRecipesForTopic(topic, settings.language)
        }
    }
}