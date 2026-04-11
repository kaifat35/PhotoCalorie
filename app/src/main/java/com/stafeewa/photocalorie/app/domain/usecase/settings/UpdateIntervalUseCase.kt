package com.stafeewa.photocalorie.app.domain.usecase.settings

import com.stafeewa.photocalorie.app.domain.entity.Interval
import com.stafeewa.photocalorie.app.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateIntervalUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(interval: Interval) {
        settingsRepository.updateInterval(interval.minutes)
    }
}