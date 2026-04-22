package com.stafeewa.photocalorie.app.presentation.workers

import com.stafeewa.photocalorie.app.domain.usecase.recipe.StartRefreshDataUseCase
import com.stafeewa.photocalorie.app.domain.usecase.settings.GetSettingsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStartupManager @Inject constructor(
    private val startRefreshDataUseCase: StartRefreshDataUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val trainingWorkScheduler: TrainingWorkScheduler
) {

    private val scope = CoroutineScope(
        Dispatchers.IO
    )

    fun startRefreshData() {
        scope.launch {
            startRefreshDataUseCase()
        }

        getSettingsUseCase()
            .distinctUntilChangedBy { "${it.trainingFrequencyHours}:${it.minTrainingExamples}" }
            .onEach { settings ->
                trainingWorkScheduler.schedulePeriodicTraining(
                    frequencyHours = settings.trainingFrequencyHours,
                    minExamples = settings.minTrainingExamples
                )
            }
            .launchIn(scope)
    }
}
