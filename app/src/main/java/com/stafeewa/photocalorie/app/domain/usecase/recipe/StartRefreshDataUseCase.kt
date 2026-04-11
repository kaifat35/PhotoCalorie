package com.stafeewa.photocalorie.app.domain.usecase.recipe

import com.stafeewa.photocalorie.app.data.mapper.toRefreshConfig
import com.stafeewa.photocalorie.app.domain.repository.RecipeRepository
import com.stafeewa.photocalorie.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class StartRefreshDataUseCase @Inject constructor(
    private val recipesRepository: RecipeRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke() {
        settingsRepository.getSettings()
            .map { it.toRefreshConfig() }
            .distinctUntilChanged()
            .onEach { recipesRepository.startBackgroundRefresh(it) }
            .collect()
    }
}