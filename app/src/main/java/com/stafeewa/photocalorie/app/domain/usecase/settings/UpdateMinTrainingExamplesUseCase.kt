package com.stafeewa.photocalorie.app.domain.usecase.settings

import com.stafeewa.photocalorie.app.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateMinTrainingExamplesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(count: Int) {
        settingsRepository.updateMinTrainingExamples(count)
    }
}
